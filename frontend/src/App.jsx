import { notifications } from "@mantine/notifications";
import { useEffect, useState } from "react";
import {
  clearSession,
  createProject,
  createTask,
  createTeam,
  deleteProject,
  deleteTask,
  deleteTeam,
  listProjects,
  listTasks,
  listTeams,
  me,
  storedUser,
  updateTask,
} from "./api";
import AuthPage from "./auth/AuthPage";
import AppShell from "./shell/AppShell";
import WorkspacePage from "./workspace/WorkspacePage";

export default function App() {
  const [user, setUser] = useState(() => storedUser());
  const [teams, setTeams] = useState([]);
  const [projects, setProjects] = useState([]);
  const [tasks, setTasks] = useState([]);
  const [teamId, setTeamId] = useState(null);
  const [projectId, setProjectId] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(Boolean(storedUser()));

  useEffect(() => {
    function loseSession() {
      setUser(null);
      setTeams([]);
      setProjects([]);
      setTasks([]);
    }
    window.addEventListener("turbo-unauthorized", loseSession);
    return () => window.removeEventListener("turbo-unauthorized", loseSession);
  }, []);

  async function refreshTeams(nextTeamId = teamId) {
    const next = await listTeams();
    setTeams(next);
    const selected = next.some((team) => team.id === nextTeamId) ? nextTeamId : next[0]?.id ?? null;
    setTeamId(selected);
    return selected;
  }

  async function refreshProjects(selectedTeamId, nextProjectId) {
    if (!selectedTeamId) {
      setProjects([]);
      setProjectId(null);
      setTasks([]);
      return null;
    }
    const next = await listProjects(selectedTeamId);
    setProjects(next);
    const selected =
      nextProjectId === null
        ? null
        : next.some((project) => project.id === nextProjectId)
          ? nextProjectId
          : (next[0]?.id ?? null);
    setProjectId(selected);
    setTasks(selected ? await listTasks(selected) : []);
    return selected;
  }

  useEffect(() => {
    if (!user) {
      setLoading(false);
      return undefined;
    }
    let active = true;

    async function load() {
      setLoading(true);
      try {
        const profile = await me();
        if (!active) {
          return;
        }
        setUser(profile);
        const nextTeams = await listTeams();
        if (!active) {
          return;
        }
        setTeams(nextTeams);
        const selectedTeam = nextTeams[0]?.id ?? null;
        setTeamId(selectedTeam);
        if (!selectedTeam) {
          setProjects([]);
          setProjectId(null);
          setTasks([]);
          return;
        }
        const nextProjects = await listProjects(selectedTeam);
        if (!active) {
          return;
        }
        setProjects(nextProjects);
        const firstProject = nextProjects[0]?.id ?? null;
        setProjectId(firstProject);
        setTasks(firstProject ? await listTasks(firstProject) : []);
      } catch (err) {
        if (active) {
          setError(err.message);
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    load();
    return () => {
      active = false;
    };
  }, [user?.id]);

  async function run(action, success) {
    setError("");
    try {
      await action();
      if (success) {
        notifications.show({ message: success, color: "violet" });
      }
      return true;
    } catch (err) {
      setError(err.message);
      notifications.show({ title: "Could not save", message: err.message, color: "red" });
      return false;
    }
  }

  if (!user) {
    return <AuthPage onSuccess={setUser} />;
  }

  return (
    <AppShell>
      <WorkspacePage
        user={user}
        teams={teams}
        projects={projects}
        tasks={tasks}
        teamId={teamId}
        projectId={projectId}
        loading={loading}
        error={error}
        onDismissError={() => setError("")}
        onLogout={() => {
          clearSession();
          setUser(null);
          setTeams([]);
          setProjects([]);
          setTasks([]);
        }}
        onSelectTeam={(id) =>
          run(async () => {
            setTeamId(id);
            await refreshProjects(id);
          })
        }
        onSelectProject={(id) =>
          run(async () => {
            setProjectId(id);
            setTasks(await listTasks(id));
          })
        }
        onCreateTeam={(body) =>
          run(async () => {
            const created = await createTeam(body);
            const selected = await refreshTeams(created.id);
            await refreshProjects(selected, null);
          }, "Team created")
        }
        onCreateProject={(body) =>
          run(async () => {
            const created = await createProject(teamId, body);
            await refreshTeams(teamId);
            await refreshProjects(teamId, created.id);
          }, "Project created")
        }
        onCreateTask={(body) =>
          run(async () => {
            const created = await createTask(projectId, {
              title: body.title,
              description: body.description,
              priority: body.priority,
              assigneeId: body.assigneeId || null,
            });
            if (body.status && body.status !== "TODO") {
              await updateTask(created.id, { status: body.status });
            }
            await refreshProjects(teamId, projectId);
          }, "Task added")
        }
        onDeleteTeam={(team) =>
          run(async () => {
            await deleteTeam(team.id);
            const selected = await refreshTeams(null);
            await refreshProjects(selected, null);
          }, "Team deleted")
        }
        onDeleteProject={(project) =>
          run(async () => {
            await deleteProject(project.id);
            await refreshTeams(teamId);
            await refreshProjects(teamId);
          }, "Project deleted")
        }
        onDeleteTask={(task) =>
          run(async () => {
            await deleteTask(task.id);
            await refreshProjects(teamId, projectId);
          }, "Task deleted")
        }
        onStatus={(task, status) =>
          run(async () => {
            await updateTask(task.id, { status });
            setTasks(await listTasks(projectId));
          })
        }
        onPriority={(task, priority) =>
          run(async () => {
            await updateTask(task.id, { priority });
            setTasks(await listTasks(projectId));
          })
        }
        onAssign={(task, assigneeId) =>
          run(async () => {
            await updateTask(task.id, { assigneeId });
            setTasks(await listTasks(projectId));
          })
        }
      />
    </AppShell>
  );
}
