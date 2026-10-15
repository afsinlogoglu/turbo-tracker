import { ActionIcon, Button, Drawer, Menu, SegmentedControl, Select, Textarea, TextInput } from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { useEffect, useMemo, useState } from "react";
import {
  addMember,
  createComment,
  deleteComment,
  listComments,
  listMembers,
  listUsers,
  removeMember,
  searchTasks,
} from "../api";
import Modal from "../components/Modal";

const COLUMNS = [
  { status: "TODO", label: "To do" },
  { status: "IN_PROGRESS", label: "Doing" },
  { status: "DONE", label: "Done" },
];

const PRIORITIES = [
  { value: "LOW", label: "Low" },
  { value: "MEDIUM", label: "Medium" },
  { value: "HIGH", label: "High" },
];

const DOTS = ["#3b82f6", "#8b5cf6", "#ec4899", "#eab308", "#f97316", "#ef4444", "#22c55e"];

function dotColor(id) {
  return DOTS[Number(id) % DOTS.length];
}

const AVATAR_COLORS = ["#6d4aff", "#0073ea", "#00c875", "#fdab3d", "#e2445c", "#a25ddc", "#ff642e"];

function initials(name) {
  if (!name) {
    return "";
  }
  return name
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((part) => part[0])
    .join("")
    .toUpperCase();
}

function avatarColor(name) {
  const text = name || "";
  let hash = 0;
  for (let index = 0; index < text.length; index += 1) {
    hash = (hash + text.charCodeAt(index) * (index + 1)) % AVATAR_COLORS.length;
  }
  return AVATAR_COLORS[hash];
}

function Avatar({ name }) {
  const letters = initials(name);
  return (
    <span
      className={letters ? "avatar" : "avatar empty"}
      title={name || "Unassigned"}
      style={letters ? { background: avatarColor(name) } : undefined}
    >
      {letters || "–"}
    </span>
  );
}

function formatWhen(value) {
  if (!value) {
    return "";
  }
  return new Intl.DateTimeFormat("en-GB", { day: "numeric", month: "short" }).format(new Date(value));
}

function healthOf(tasks) {
  const open = tasks.filter((task) => task.status !== "DONE");
  if (open.some((task) => task.priority === "HIGH")) {
    return { label: "At Risk", tone: "risk" };
  }
  if (tasks.some((task) => task.status === "IN_PROGRESS") && open.length > tasks.length / 2) {
    return { label: "Off Track", tone: "off" };
  }
  return { label: "On Track", tone: "track" };
}

function Icon({ name }) {
  const common = {
    viewBox: "0 0 24 24",
    fill: "none",
    stroke: "currentColor",
    strokeWidth: "1.8",
    strokeLinecap: "round",
    strokeLinejoin: "round",
    "aria-hidden": true,
  };
  if (name === "home") {
    return (
      <svg {...common}>
        <path d="M4 10.5 12 4l8 6.5V20a1 1 0 0 1-1 1h-5v-6H10v6H5a1 1 0 0 1-1-1z" />
      </svg>
    );
  }
  if (name === "check") {
    return (
      <svg {...common}>
        <circle cx="12" cy="12" r="8" />
        <path d="m9 12 2 2 4-4" />
      </svg>
    );
  }
  if (name === "bell") {
    return (
      <svg {...common}>
        <path d="M6 9a6 6 0 1 1 12 0c0 7 3 7 3 7H3s3 0 3-7" />
        <path d="M10 19a2 2 0 0 0 4 0" />
      </svg>
    );
  }
  if (name === "chart") {
    return (
      <svg {...common}>
        <path d="M4 19V5" />
        <path d="M4 19h16" />
        <path d="M8 16v-5" />
        <path d="M12 16V8" />
        <path d="M16 16v-3" />
      </svg>
    );
  }
  if (name === "target") {
    return (
      <svg {...common}>
        <circle cx="12" cy="12" r="8" />
        <circle cx="12" cy="12" r="3" />
      </svg>
    );
  }
  if (name === "search") {
    return (
      <svg {...common}>
        <circle cx="11" cy="11" r="6" />
        <path d="m20 20-3.5-3.5" />
      </svg>
    );
  }
  if (name === "plus") {
    return (
      <svg {...common}>
        <path d="M12 5v14M5 12h14" />
      </svg>
    );
  }
  if (name === "star") {
    return (
      <svg {...common}>
        <path d="m12 4 2.2 4.6 5 .7-3.6 3.5.9 5.1L12 15.8 7.5 17.9l.9-5.1L4.8 9.3l5-.7z" />
      </svg>
    );
  }
  if (name === "menu") {
    return (
      <svg {...common}>
        <path d="M5 7h14M5 12h14M5 17h14" />
      </svg>
    );
  }
  if (name === "user") {
    return (
      <svg {...common}>
        <circle cx="12" cy="8" r="3" />
        <path d="M5 19c1.5-3 3.8-4.5 7-4.5S17.5 16 19 19" />
      </svg>
    );
  }
  if (name === "more") {
    return (
      <svg {...common}>
        <circle cx="6" cy="12" r="1" fill="currentColor" stroke="none" />
        <circle cx="12" cy="12" r="1" fill="currentColor" stroke="none" />
        <circle cx="18" cy="12" r="1" fill="currentColor" stroke="none" />
      </svg>
    );
  }
  return (
    <svg {...common}>
      <rect x="5" y="5" width="6" height="6" rx="1.5" />
      <rect x="13" y="5" width="6" height="6" rx="1.5" />
      <rect x="5" y="13" width="6" height="6" rx="1.5" />
      <rect x="13" y="13" width="6" height="6" rx="1.5" />
    </svg>
  );
}

export default function WorkspacePage({
  user,
  teams,
  projects,
  tasks,
  teamId,
  projectId,
  loading,
  error,
  onDismissError,
  onLogout,
  onSelectTeam,
  onSelectProject,
  onCreateTeam,
  onCreateProject,
  onCreateTask,
  onDeleteTeam,
  onDeleteProject,
  onDeleteTask,
  onStatus,
  onPriority,
  onAssign,
}) {
  const [view, setView] = useState("board");
  const [query, setQuery] = useState("");
  const [priority, setPriority] = useState("ALL");
  const [sort, setSort] = useState("newest");
  const [collapsed, setCollapsed] = useState(false);
  const [navOpen, setNavOpen] = useState(false);
  const [dialog, setDialog] = useState(null);
  const [confirm, setConfirm] = useState(null);
  const [composer, setComposer] = useState(null);
  const [saving, setSaving] = useState(false);
  const [panelError, setPanelError] = useState("");
  const [selectedId, setSelectedId] = useState(null);
  const [draggingId, setDraggingId] = useState(null);
  const [members, setMembers] = useState([]);
  const [people, setPeople] = useState([]);
  const [comments, setComments] = useState([]);
  const [commentDraft, setCommentDraft] = useState("");
  const [assigneeFilter, setAssigneeFilter] = useState("ALL");
  const [page, setPage] = useState(0);
  const [searchPage, setSearchPage] = useState(null);
  const [memberUserId, setMemberUserId] = useState("");
  const [memberRole, setMemberRole] = useState("MEMBER");
  const [accountOpen, setAccountOpen] = useState(false);
  const [projectMenu, setProjectMenu] = useState(false);
  const team = teams.find((item) => item.id === teamId) ?? null;
  const project = projects.find((item) => item.id === projectId) ?? null;
  const health = healthOf(tasks);
  const isAdmin = user?.role === "ADMIN";
  const myMembership = members.find((member) => member.userId === user?.id);
  const canManage = isAdmin || myMembership?.role === "OWNER";

  const visibleTasks = useMemo(() => {
    const needle = query.trim().toLocaleLowerCase("tr");
    const rank = { HIGH: 0, MEDIUM: 1, LOW: 2 };
    return tasks
      .filter((task) => (priority === "ALL" ? true : task.priority === priority))
      .filter((task) => {
        if (assigneeFilter === "ALL") {
          return true;
        }
        if (assigneeFilter === "NONE") {
          return !task.assigneeId;
        }
        return task.assigneeId === Number(assigneeFilter);
      })
      .filter((task) => (needle ? task.title.toLocaleLowerCase("tr").includes(needle) : true))
      .slice()
      .sort((a, b) => {
        if (sort === "title") {
          return a.title.localeCompare(b.title, "tr");
        }
        if (sort === "priority") {
          return rank[a.priority] - rank[b.priority];
        }
        return new Date(b.createdAt) - new Date(a.createdAt);
      });
  }, [tasks, query, priority, sort, assigneeFilter]);

  const selectedTask = tasks.find((task) => task.id === selectedId) ?? null;

  useEffect(() => {
    if (!projectId) {
      setMembers([]);
      return undefined;
    }
    let active = true;
    Promise.all([listMembers(projectId), listUsers()])
      .then(([nextMembers, nextUsers]) => {
        if (!active) {
          return;
        }
        setMembers(nextMembers);
        setPeople(nextUsers);
        setMemberUserId(nextUsers[0]?.id ? String(nextUsers[0].id) : "");
      })
      .catch(() => {
        if (active) {
          setMembers([]);
        }
      });
    return () => {
      active = false;
    };
  }, [projectId]);

  useEffect(() => {
    if (!selectedTask) {
      setComments([]);
      return undefined;
    }
    let active = true;
    listComments(selectedTask.id)
      .then((next) => {
        if (active) {
          setComments(next);
        }
      })
      .catch(() => {
        if (active) {
          setComments([]);
        }
      });
    return () => {
      active = false;
    };
  }, [selectedTask?.id]);

  useEffect(() => {
    if (view !== "list" || !projectId) {
      return undefined;
    }
    let active = true;
    searchTasks({
      projectId,
      q: query.trim(),
      priority: priority === "ALL" ? undefined : priority,
      sort,
      page,
      size: 8,
      assigneeId: assigneeFilter === "ALL" || assigneeFilter === "NONE" ? undefined : assigneeFilter,
      unassigned: assigneeFilter === "NONE",
    })
      .then((result) => {
        if (active) {
          setSearchPage(result);
        }
      })
      .catch(() => {
        if (active) {
          setSearchPage({ content: [], page: 0, size: 8, totalElements: 0, totalPages: 0 });
        }
      });
    return () => {
      active = false;
    };
  }, [view, projectId, query, priority, sort, page, assigneeFilter, tasks]);

  async function submit(action) {
    setSaving(true);
    setPanelError("");
    try {
      const ok = await action();
      if (ok) {
        setDialog(null);
        setConfirm(null);
        setComposer(null);
      }
    } catch (err) {
      setPanelError(err.message);
      notifications.show({ title: "Could not save", message: err.message, color: "red" });
    } finally {
      setSaving(false);
    }
  }

  function openComposer(status) {
    setComposer(status);
    setPanelError("");
  }

  return (
    <div className={`${collapsed ? "workspace is-collapsed" : "workspace"}${navOpen ? " nav-open" : ""}`}>
      {navOpen ? (
        <button type="button" className="scrim" aria-label="Menüyü kapat" onClick={() => setNavOpen(false)} />
      ) : null}
      <aside className="sidebar">
        <div className="brand-row">
          <div className="brand">
            <span className="logo" aria-hidden="true">
              <i />
              <i />
              <i />
              <i />
            </span>
            <strong>turbo</strong>
          </div>
          <button type="button" className="icon-btn desktop-only" aria-label="Menüyü daralt" onClick={() => setCollapsed((value) => !value)}>
            <Icon name="menu" />
          </button>
        </div>

        {!loading && teams.length > 0 ? (
          <label className="team-switch">
            <span className="sr">Takım</span>
            <select value={teamId ?? ""} onChange={(event) => onSelectTeam(Number(event.target.value))}>
              {teams.map((item) => (
                <option key={item.id} value={item.id}>
                  {item.name}
                </option>
              ))}
            </select>
          </label>
        ) : null}
        {!loading && teams.length === 0 ? <p className="subtle side-note">No team yet</p> : null}

        <div className="favorites">
          <div className="section-head">
            <p>Projects</p>
            <button type="button" className="icon-btn" aria-label="New project" disabled={!team} onClick={() => setDialog("project")}>
              <Icon name="plus" />
            </button>
          </div>
          <ul>
            {projects.map((item) => (
              <li key={item.id}>
                <button
                  type="button"
                  className={item.id === projectId ? "fav active" : "fav"}
                  onClick={() => {
                    onSelectProject(item.id);
                    setNavOpen(false);
                  }}
                >
                  <i style={{ background: dotColor(item.id) }} />
                  <span>{item.name}</span>
                </button>
              </li>
            ))}
          </ul>
          {projects.length === 0 && !loading ? <p className="subtle side-note">No projects yet.</p> : null}
        </div>

        <div className="side-foot">
          {isAdmin ? (
            <button type="button" className="nav-item" onClick={() => setDialog("team")}>
              <Icon name="plus" />
              <span>New team</span>
            </button>
          ) : null}
          <button type="button" className="account" onClick={() => setAccountOpen((value) => !value)}>
            <Avatar name={user?.name} />
            <span>
              <strong>{user?.name}</strong>
              <em className={isAdmin ? "role admin" : "role member"}>{isAdmin ? "Admin" : "Member"}</em>
            </span>
          </button>
          {accountOpen ? (
            <button type="button" className="secondary account-logout" onClick={onLogout}>
              Log out
            </button>
          ) : null}
        </div>
      </aside>

      <main className="stage">
        <header className="project-head">
          <div className="project-title">
            <span className="project-mark" aria-hidden="true">
              <Icon name="board" />
            </span>
            <div>
              <div className="title-line">
                <h1>{loading ? "Loading" : project ? project.name : team ? "Select a project" : "Create a team"}</h1>
                {project && canManage ? (
                  <div className="more">
                    <button
                      type="button"
                      className="icon-btn"
                      aria-label="Project actions"
                      aria-expanded={projectMenu}
                      onClick={() => setProjectMenu((value) => !value)}
                    >
                      <Icon name="more" />
                    </button>
                    {projectMenu ? (
                      <div className="menu" role="menu">
                        <button
                          type="button"
                          className="menu-item"
                          onClick={() => {
                            setProjectMenu(false);
                            setConfirm({ kind: "project", item: project });
                          }}
                        >
                          Delete project
                        </button>
                        {isAdmin && team ? (
                          <button
                            type="button"
                            className="menu-item"
                            onClick={() => {
                              setProjectMenu(false);
                              setConfirm({ kind: "team", item: team });
                            }}
                          >
                            Delete team
                          </button>
                        ) : null}
                      </div>
                    ) : null}
                  </div>
                ) : null}
              </div>
              {project ? (
                <p className={`health ${health.tone}`}>
                  <i />
                  {health.label}
                </p>
              ) : (
                <p className="subtle">{team ? "Pick a project or create one." : "Start with a team, then add a project."}</p>
              )}
            </div>
          </div>
          <div className="head-tools">
            <button type="button" className="icon-btn menu-toggle" aria-label="Menüyü aç" onClick={() => setNavOpen(true)}>
              <Icon name="menu" />
            </button>
            <label className="search">
              <Icon name="search" />
              <input
                value={query}
                onChange={(event) => {
                  setQuery(event.target.value);
                  setPage(0);
                }}
                placeholder="Search tasks"
                aria-label="Search tasks"
              />
            </label>
            <button type="button" className="primary" onClick={() => openComposer("TODO")} disabled={!project}>
              <Icon name="plus" />
              Add task
            </button>
          </div>
        </header>

        <div className="view-bar">
          <SegmentedControl
            value={view}
            onChange={setView}
            data={[
              { label: "Overview", value: "overview" },
              { label: "List", value: "list" },
              { label: "Board", value: "board" },
            ]}
          />
        </div>

        <div className="toolbar">
          <Select
            aria-label="Priority filter"
            placeholder="Priority"
            value={priority}
            allowDeselect={false}
            onChange={(value) => {
              setPriority(value);
              setPage(0);
            }}
            data={[{ value: "ALL", label: "All priorities" }, ...PRIORITIES]}
            size="sm"
            w={160}
          />
          <Select
            aria-label="Assignee filter"
            placeholder="Person"
            value={String(assigneeFilter)}
            allowDeselect={false}
            onChange={(value) => {
              setAssigneeFilter(value);
              setPage(0);
            }}
            data={[
              { value: "ALL", label: "Everyone" },
              { value: "NONE", label: "Unassigned" },
              ...members.map((member) => ({ value: String(member.userId), label: member.name })),
            ]}
            size="sm"
            w={160}
          />
          <Select
            aria-label="Sort"
            value={sort}
            allowDeselect={false}
            onChange={(value) => {
              setSort(value);
              setPage(0);
            }}
            data={[
              { value: "newest", label: "Newest" },
              { value: "title", label: "Title" },
              { value: "priority", label: "Priority" },
            ]}
            size="sm"
            w={140}
          />
        </div>

        {error ? (
          <p className="banner" role="alert">
            {error}
            <button type="button" onClick={onDismissError} aria-label="Kapat">
              ×
            </button>
          </p>
        ) : null}

        {loading ? <p className="muted pad">Loading…</p> : null}

        {!loading && !project ? (
          <section className="empty-stage">
            <h2>{team ? "Create the first project" : "Create a team to start"}</h2>
            <p>Projects stay in the sidebar. Tasks live on the board.</p>
            <button type="button" className="primary" onClick={() => setDialog(team ? "project" : "team")}>
              {team ? "New project" : "New team"}
            </button>
          </section>
        ) : null}

        {project && view === "overview" ? (
          <section className="overview">
            <p className="lede">{project.description || "No description yet."}</p>
            <div className="stats">
              {COLUMNS.map((column) => (
                <article key={column.status}>
                  <span>{column.label}</span>
                  <strong>{tasks.filter((task) => task.status === column.status).length}</strong>
                </article>
              ))}
            </div>
            <div className="members">
              <header>
                <h2>Members</h2>
                {canManage ? (
                  <form
                    onSubmit={(event) => {
                      event.preventDefault();
                      if (!memberUserId) {
                        return;
                      }
                      submit(async () => {
                        await addMember(projectId, { userId: Number(memberUserId), role: memberRole });
                        setMembers(await listMembers(projectId));
                        notifications.show({ message: "Member added", color: "violet" });
                        return true;
                      });
                    }}
                  >
                    <Select
                      aria-label="User"
                      value={memberUserId ? String(memberUserId) : null}
                      allowDeselect={false}
                      onChange={setMemberUserId}
                      data={people.map((person) => ({ value: String(person.id), label: person.name }))}
                      size="sm"
                      w={160}
                    />
                    <Select
                      aria-label="Project role"
                      value={memberRole}
                      allowDeselect={false}
                      onChange={setMemberRole}
                      data={[
                        { value: "MEMBER", label: "Member" },
                        { value: "OWNER", label: "Owner" },
                      ]}
                      size="sm"
                      w={130}
                    />
                    <Button type="submit" size="sm" loading={saving} disabled={!memberUserId}>
                      Add
                    </Button>
                  </form>
                ) : null}
              </header>
              {panelError ? <p className="form-error">{panelError}</p> : null}
              <ul>
                {members.map((member) => (
                  <li key={member.userId}>
                    <Avatar name={member.name} />
                    <span>
                      <strong>{member.name}</strong>
                      <small>{member.email}</small>
                    </span>
                    <em className={member.role === "OWNER" ? "role admin" : "role member"}>
                      {member.role === "OWNER" ? "Owner" : "Member"}
                    </em>
                    {canManage ? (
                      <Menu position="bottom-end" withinPortal>
                        <Menu.Target>
                          <ActionIcon variant="subtle" color="gray" aria-label={`${member.name} actions`}>
                            <Icon name="more" />
                          </ActionIcon>
                        </Menu.Target>
                        <Menu.Dropdown>
                          <Menu.Item color="red" onClick={() => setConfirm({ kind: "member", item: member })}>
                            Remove from project
                          </Menu.Item>
                        </Menu.Dropdown>
                      </Menu>
                    ) : null}
                  </li>
                ))}
              </ul>
            </div>
          </section>
        ) : null}

        {project && view === "list" ? (
          <section className="list">
            {searchPage && searchPage.content.length === 0 ? <p className="muted">No tasks match.</p> : null}
            {(searchPage?.content ?? []).map((task) => (
              <article key={task.id} className="list-row">
                <button
                  type="button"
                  className={task.status === "DONE" ? "tick done" : "tick"}
                  aria-label={`${task.title} tamamla`}
                  onClick={() => onStatus(task, task.status === "DONE" ? "TODO" : "DONE")}
                />
                <button type="button" className="row-title" onClick={() => setSelectedId(task.id)}>
                  {task.title}
                </button>
                <button type="button" className={`pill ${task.priority.toLowerCase()}`} onClick={() => cyclePriority(task, onPriority)}>
                  {labelOf(task.priority)}
                </button>
                <Avatar name={task.assigneeName} />
                <Select
                  aria-label={`${task.title} durumu`}
                  value={task.status}
                  allowDeselect={false}
                  onChange={(value) => onStatus(task, value)}
                  data={COLUMNS.map((column) => ({ value: column.status, label: column.label }))}
                  size="xs"
                  w={120}
                />
              </article>
            ))}
            <div className="pager">
              <button type="button" className="secondary" disabled={page === 0} onClick={() => setPage((value) => value - 1)}>
                Previous
              </button>
              <span>
                Page {(searchPage?.page ?? 0) + 1} of {Math.max(searchPage?.totalPages ?? 0, 1)} · {searchPage?.totalElements ?? 0} tasks
              </span>
              <button
                type="button"
                className="secondary"
                disabled={!searchPage || page + 1 >= searchPage.totalPages}
                onClick={() => setPage((value) => value + 1)}
              >
                Next
              </button>
            </div>
          </section>
        ) : null}

        {project && view === "board" ? (
          <section className="board" aria-label="Board">
            {COLUMNS.map((column) => {
              const cards = visibleTasks.filter((task) => task.status === column.status);
              return (
                <div
                  key={column.status}
                  className={draggingId ? "column hot" : "column"}
                  onDragOver={(event) => event.preventDefault()}
                  onDrop={(event) => {
                    event.preventDefault();
                    const id = Number(event.dataTransfer.getData("text/plain"));
                    const task = tasks.find((item) => item.id === id);
                    setDraggingId(null);
                    if (task && task.status !== column.status) {
                      onStatus(task, column.status);
                    }
                  }}
                >
                  <header>
                    <h2>
                      <i />
                      {column.label}
                    </h2>
                    <span>{cards.length}</span>
                  </header>
                  <div className="cards">
                    {cards.map((task) => (
                      <article
                        key={task.id}
                        className={selectedId === task.id ? "card lifted" : "card"}
                        draggable
                        onDragStart={(event) => {
                          event.dataTransfer.setData("text/plain", String(task.id));
                          event.dataTransfer.effectAllowed = "move";
                          setDraggingId(task.id);
                        }}
                        onDragEnd={() => setDraggingId(null)}
                        onClick={() => setSelectedId(task.id)}
                      >
                        <div className="card-top">
                          <button
                            type="button"
                            className={task.status === "DONE" ? "tick done" : "tick"}
                            aria-label={`${task.title} tamamla`}
                            onClick={(event) => {
                              event.stopPropagation();
                              onStatus(task, task.status === "DONE" ? "TODO" : "DONE");
                            }}
                          />
                          <h3>{task.title}</h3>
                        </div>
                        <div className="card-meta">
                          <button
                            type="button"
                            className={`pill ${task.priority.toLowerCase()}`}
                            onClick={(event) => {
                              event.stopPropagation();
                              cyclePriority(task, onPriority);
                            }}
                          >
                            {labelOf(task.priority)}
                          </button>
                          <time>{formatWhen(task.createdAt)}</time>
                          <Avatar name={task.assigneeName} />
                        </div>
                      </article>
                    ))}
                    <button type="button" className="add-card" onClick={() => openComposer(column.status)}>
                      <Icon name="plus" />
                      Add task
                    </button>
                  </div>
                </div>
              );
            })}
          </section>
        ) : null}
      </main>

      <Drawer
        opened={Boolean(selectedTask)}
        onClose={() => setSelectedId(null)}
        position="right"
        size="md"
        title={selectedTask?.title}
        radius="lg"
        offset={12}
      >
        {selectedTask ? (
          <>
          <section className="detail-block">
            <h3>Assignee</h3>
            <PeoplePicker
              members={members}
              value={selectedTask.assigneeId ?? null}
              onChange={(assigneeId) => onAssign(selectedTask, assigneeId)}
            />
          </section>
          <section className="detail-block comments">
            <h3>Comments</h3>
            {panelError ? <p className="form-error">{panelError}</p> : null}
            {comments.length === 0 ? <p className="muted">No comments yet.</p> : null}
            <ul>
              {comments.map((comment) => (
                <li key={comment.id} className="comment">
                  <Avatar name={comment.authorName} />
                  <div>
                    <header>
                      <strong>{comment.authorName}</strong>
                      <time>{formatWhen(comment.createdAt)}</time>
                      {user && (user.role === "ADMIN" || user.id === comment.authorId) ? (
                        <ActionIcon
                          variant="subtle"
                          color="gray"
                          aria-label="Delete comment"
                          onClick={() => setConfirm({ kind: "comment", item: comment })}
                        >
                          <Icon name="more" />
                        </ActionIcon>
                      ) : null}
                    </header>
                    <p>{comment.body}</p>
                  </div>
                </li>
              ))}
            </ul>
            <form
              className="comment-box"
              onSubmit={(event) => {
                event.preventDefault();
                const body = commentDraft.trim();
                if (!body) {
                  return;
                }
                submit(async () => {
                  await createComment(selectedTask.id, body);
                  setComments(await listComments(selectedTask.id));
                  setCommentDraft("");
                  notifications.show({ message: "Comment sent", color: "violet" });
                  return false;
                });
              }}
            >
              <Textarea
                value={commentDraft}
                onChange={(event) => setCommentDraft(event.target.value)}
                minRows={3}
                placeholder="Write a comment"
                aria-label="Comment"
              />
              <Button type="submit" loading={saving} disabled={!commentDraft.trim()}>
                Send
              </Button>
            </form>
          </section>
          <Button
            mt="md"
            fullWidth
            variant="light"
            color="red"
            onClick={() => setConfirm({ kind: "task", item: selectedTask })}
          >
            Delete task
          </Button>
          </>
        ) : null}
      </Drawer>

      {composer ? (
        <TaskDialog
          status={composer}
          members={members}
          saving={saving}
          error={panelError}
          onClose={() => setComposer(null)}
          onSubmit={(body) => submit(() => onCreateTask(body))}
        />
      ) : null}
      {dialog === "team" ? (
        <FormDialog
          title="New team"
          nameLabel="Team name"
          saving={saving}
          onClose={() => setDialog(null)}
          onSubmit={(body) => submit(() => onCreateTeam(body))}
          error={error}
        />
      ) : null}
      {dialog === "project" ? (
        <FormDialog
          title="New project"
          nameLabel="Project name"
          saving={saving}
          onClose={() => setDialog(null)}
          onSubmit={(body) => submit(() => onCreateProject(body))}
          error={error}
        />
      ) : null}
      {confirm ? (
        <Modal
          title={
            {
              project: "Delete project",
              team: "Delete team",
              task: "Delete task",
              member: "Remove member",
              comment: "Delete comment",
            }[confirm.kind]
          }
          onClose={() => setConfirm(null)}
          footer={
            <>
              <Button variant="default" onClick={() => setConfirm(null)}>
                Cancel
              </Button>
              <Button
                color="red"
                loading={saving}
                onClick={() =>
                  submit(async () => {
                    if (confirm.kind === "project") {
                      return onDeleteProject(confirm.item);
                    }
                    if (confirm.kind === "team") {
                      return onDeleteTeam(confirm.item);
                    }
                    if (confirm.kind === "task") {
                      const ok = await onDeleteTask(confirm.item);
                      if (ok) {
                        setSelectedId(null);
                      }
                      return ok;
                    }
                    if (confirm.kind === "member") {
                      await removeMember(projectId, confirm.item.userId);
                      setMembers(await listMembers(projectId));
                      notifications.show({ message: "Member removed", color: "violet" });
                      return true;
                    }
                    await deleteComment(confirm.item.id);
                    if (selectedTask) {
                      setComments(await listComments(selectedTask.id));
                    }
                    notifications.show({ message: "Comment deleted", color: "violet" });
                    return false;
                  })
                }
              >
                {confirm.kind === "member" ? "Remove" : "Delete"}
              </Button>
            </>
          }
        >
          <p>{confirmCopy(confirm)}</p>
          {error ? <p className="form-error">{error}</p> : null}
        </Modal>
      ) : null}
    </div>
  );
}

function confirmCopy(confirm) {
  if (confirm.kind === "task") {
    return `Delete “${confirm.item.title}”? This cannot be undone.`;
  }
  if (confirm.kind === "member") {
    return `Remove ${confirm.item.name} from this project?`;
  }
  if (confirm.kind === "comment") {
    return "Delete this comment?";
  }
  return `${confirm.item.name} will be removed. A project that still has tasks, or a team that still has projects, stays in place.`;
}

function labelOf(value) {
  return PRIORITIES.find((item) => item.value === value)?.label ?? value;
}

function cyclePriority(task, onPriority) {
  const order = ["LOW", "MEDIUM", "HIGH"];
  const next = order[(order.indexOf(task.priority) + 1) % order.length];
  onPriority(task, next);
}

function PeoplePicker({ members, value, onChange }) {
  return (
    <div className="people" role="listbox" aria-label="Assignee">
      <button type="button" className={value ? "person" : "person active"} onClick={() => onChange(null)}>
        <span className="avatar empty">–</span>
        <span>Unassigned</span>
      </button>
      {members.map((member) => (
        <button
          type="button"
          key={member.userId}
          className={value === member.userId ? "person active" : "person"}
          onClick={() => onChange(member.userId)}
        >
          <Avatar name={member.name} />
          <span>{member.name}</span>
        </button>
      ))}
    </div>
  );
}

function TaskDialog({ status, members, saving, error, onClose, onSubmit }) {
  const [title, setTitle] = useState("");
  const [priority, setPriority] = useState("MEDIUM");
  const [assigneeId, setAssigneeId] = useState(null);
  const column = COLUMNS.find((item) => item.status === status);

  return (
    <Modal
      title="New task"
      onClose={onClose}
      footer={
        <>
          <Button variant="default" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" form="task-form" loading={saving} disabled={!title.trim()}>
            Add task
          </Button>
        </>
      }
    >
      <form
        id="task-form"
        onSubmit={(event) => {
          event.preventDefault();
          onSubmit({ title: title.trim(), priority, status, assigneeId });
        }}
      >
        <p className="subtle">This task goes to {column?.label || "To do"}.</p>
        <TextInput
          label="Task name"
          value={title}
          onChange={(event) => setTitle(event.target.value)}
          placeholder="What needs doing?"
          data-autofocus
          mt="sm"
        />
        <div className="field-label">Priority</div>
        <div className="segment" role="radiogroup" aria-label="Priority">
          {PRIORITIES.map((item) => (
            <button
              key={item.value}
              type="button"
              className={priority === item.value ? `segment-btn ${item.value.toLowerCase()} active` : "segment-btn"}
              onClick={() => setPriority(item.value)}
            >
              {item.label}
            </button>
          ))}
        </div>
        <div className="field-label">Assignee</div>
        <PeoplePicker members={members} value={assigneeId} onChange={setAssigneeId} />
        {error ? <p className="form-error">{error}</p> : null}
      </form>
    </Modal>
  );
}

function FormDialog({ title, nameLabel, saving, error, onClose, onSubmit }) {
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");

  return (
    <Modal
      title={title}
      onClose={onClose}
      footer={
        <>
          <Button variant="default" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" form="entity-form" loading={saving} disabled={!name.trim()}>
            Create
          </Button>
        </>
      }
    >
      <form
        id="entity-form"
        onSubmit={(event) => {
          event.preventDefault();
          onSubmit({ name: name.trim(), description: description.trim() });
        }}
      >
        <TextInput label={nameLabel} value={name} onChange={(event) => setName(event.target.value)} autoFocus data-autofocus />
        <Textarea label="Description" value={description} onChange={(event) => setDescription(event.target.value)} minRows={3} mt="md" />
        {error ? <p className="form-error">{error}</p> : null}
      </form>
    </Modal>
  );
}
