package com.turbo.tracker.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.turbo.tracker.entity.Comment;
import com.turbo.tracker.entity.Project;
import com.turbo.tracker.entity.ProjectMember;
import com.turbo.tracker.entity.ProjectRole;
import com.turbo.tracker.entity.Role;
import com.turbo.tracker.entity.Task;
import com.turbo.tracker.entity.TaskPriority;
import com.turbo.tracker.entity.TaskStatus;
import com.turbo.tracker.entity.Team;
import com.turbo.tracker.entity.User;
import com.turbo.tracker.repository.CommentRepository;
import com.turbo.tracker.repository.ProjectMemberRepository;
import com.turbo.tracker.repository.ProjectRepository;
import com.turbo.tracker.repository.TaskRepository;
import com.turbo.tracker.repository.TeamRepository;
import com.turbo.tracker.repository.UserRepository;

@Component
@ConditionalOnProperty(prefix = "turbo.seed", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseSeeder implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

	private final UserRepository users;
	private final TeamRepository teams;
	private final ProjectRepository projects;
	private final ProjectMemberRepository members;
	private final TaskRepository tasks;
	private final CommentRepository comments;
	private final PasswordEncoder passwords;

	public DatabaseSeeder(
			UserRepository users,
			TeamRepository teams,
			ProjectRepository projects,
			ProjectMemberRepository members,
			TaskRepository tasks,
			CommentRepository comments,
			PasswordEncoder passwords) {
		this.users = users;
		this.teams = teams;
		this.projects = projects;
		this.members = members;
		this.tasks = tasks;
		this.comments = comments;
		this.passwords = passwords;
	}

	@Override
	@Transactional
	public void run(String... args) {
		if (teams.count() > 0) {
			return;
		}

		User ada = users.save(new User("Ada", "ada@turbo.dev", passwords.encode("password"), Role.ADMIN));
		User leo = users.save(new User("Leo", "leo@turbo.dev", passwords.encode("password"), Role.MEMBER));

		Team platform = teams.save(new Team("Platform", "Product and engineering"));
		Project stories = projects.save(new Project(platform, "Customer Stories - Q4", "Quarterly customer story board"));
		Project launch = projects.save(new Project(platform, "Website Launch", "Public site for the new release"));
		membership(stories, ada, leo);
		membership(launch, ada, leo);

		Task meeting = task(stories, "Hold introductory meeting", "Walk through the draft narrative", TaskPriority.MEDIUM, TaskStatus.IN_PROGRESS, ada);
		task(stories, "Contact client for outline", "Confirm the story angle with the account team", TaskPriority.MEDIUM, TaskStatus.TODO, leo);
		task(stories, "Collect quotes", "Pull two customer quotes from last quarter", TaskPriority.LOW, TaskStatus.TODO, null);
		task(stories, "Set final deadline", "Lock the publish date with marketing", TaskPriority.HIGH, TaskStatus.IN_PROGRESS, ada);
		task(stories, "Determine project goal", "Agree the one outcome this story should drive", TaskPriority.LOW, TaskStatus.DONE, leo);
		comments.save(new Comment(meeting, ada, "Thursday, 30 minutes. Leo owns the notes."));

		Task hero = task(launch, "Write homepage hero", "One sentence, no product jargon", TaskPriority.HIGH, TaskStatus.TODO, leo);
		task(launch, "Review launch checklist", "DNS, analytics, and status page", TaskPriority.MEDIUM, TaskStatus.IN_PROGRESS, ada);
		task(launch, "Publish release notes", "Ship the notes with the homepage", TaskPriority.LOW, TaskStatus.DONE, ada);
		comments.save(new Comment(hero, leo, "Draft is in the doc. Need a tighter verb."));

		Team growth = teams.save(new Team("Growth", "Campaigns and lifecycle"));
		Project marketing = projects.save(new Project(growth, "Marketing", "Always-on campaign board"));
		membership(marketing, ada, leo);
		task(marketing, "Plan email calendar", "Four sends for the quarter", TaskPriority.MEDIUM, TaskStatus.TODO, leo);
		task(marketing, "Cut rebranding video", "30 second cut for the homepage", TaskPriority.HIGH, TaskStatus.IN_PROGRESS, ada);

		log.info("Seeded demo users, teams, projects, tasks, and comments");
	}

	private void membership(Project project, User owner, User member) {
		members.save(new ProjectMember(project, owner, ProjectRole.OWNER));
		members.save(new ProjectMember(project, member, ProjectRole.MEMBER));
	}

	private Task task(Project project, String title, String description, TaskPriority priority, TaskStatus status, User assignee) {
		Task task = tasks.save(new Task(project, title, description, priority));
		task.changeStatus(status);
		task.assign(assignee);
		return task;
	}

}
