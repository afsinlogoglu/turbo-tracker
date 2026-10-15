const JSON_HEADERS = { "Content-Type": "application/json" };
const TOKEN_KEY = "turbo.token";
const USER_KEY = "turbo.user";

export function storedUser() {
  const raw = localStorage.getItem(USER_KEY);
  if (!raw) {
    return null;
  }
  try {
    return JSON.parse(raw);
  } catch {
    return null;
  }
}

export function saveSession(auth) {
  localStorage.setItem(TOKEN_KEY, auth.token);
  localStorage.setItem(USER_KEY, JSON.stringify(auth.user));
}

export function clearSession() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

export async function request(path, options = {}) {
  const headers = { ...JSON_HEADERS, ...options.headers };
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(path, { ...options, headers });
  if (response.status === 204) {
    return null;
  }

  const body = await response.json().catch(() => null);
  const isAuthAttempt = path.startsWith("/api/auth/login") || path.startsWith("/api/auth/register");
  if (response.status === 401 && !isAuthAttempt) {
    clearSession();
    window.dispatchEvent(new Event("turbo-unauthorized"));
  }
  if (!response.ok) {
    throw new Error(body?.message || `Request failed (${response.status})`);
  }
  return body;
}

export const register = (body) =>
  request("/api/auth/register", { method: "POST", body: JSON.stringify(body) });
export const login = (body) =>
  request("/api/auth/login", { method: "POST", body: JSON.stringify(body) });
export const me = () => request("/api/auth/me");

export const listUsers = () => request("/api/users");
export const listTeams = () => request("/api/teams");
export const createTeam = (body) =>
  request("/api/teams", { method: "POST", body: JSON.stringify(body) });
export const deleteTeam = (id) => request(`/api/teams/${id}`, { method: "DELETE" });

export const listProjects = (teamId) => request(`/api/teams/${teamId}/projects`);
export const createProject = (teamId, body) =>
  request(`/api/teams/${teamId}/projects`, { method: "POST", body: JSON.stringify(body) });
export const deleteProject = (id) => request(`/api/projects/${id}`, { method: "DELETE" });

export const listMembers = (projectId) => request(`/api/projects/${projectId}/members`);
export const addMember = (projectId, body) =>
  request(`/api/projects/${projectId}/members`, { method: "POST", body: JSON.stringify(body) });
export const removeMember = (projectId, userId) =>
  request(`/api/projects/${projectId}/members/${userId}`, { method: "DELETE" });

export const listTasks = (projectId) => request(`/api/projects/${projectId}/tasks`);
export const createTask = (projectId, body) =>
  request(`/api/projects/${projectId}/tasks`, { method: "POST", body: JSON.stringify(body) });
export const updateTask = (id, patch) =>
  request(`/api/tasks/${id}`, { method: "PATCH", body: JSON.stringify(patch) });
export const deleteTask = (id) => request(`/api/tasks/${id}`, { method: "DELETE" });

export const listComments = (taskId) => request(`/api/tasks/${taskId}/comments`);
export const createComment = (taskId, body) =>
  request(`/api/tasks/${taskId}/comments`, { method: "POST", body: JSON.stringify({ body }) });
export const deleteComment = (id) => request(`/api/comments/${id}`, { method: "DELETE" });

export function searchTasks(params) {
  const query = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "" && value !== false) {
      query.set(key, String(value));
    }
  });
  if (params.unassigned) {
    query.set("unassigned", "true");
  }
  return request(`/api/search/tasks?${query.toString()}`);
}
