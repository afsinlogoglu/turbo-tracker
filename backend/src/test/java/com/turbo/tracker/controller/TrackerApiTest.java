package com.turbo.tracker.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.jayway.jsonpath.JsonPath;
import com.turbo.tracker.repository.CommentRepository;
import com.turbo.tracker.repository.ProjectMemberRepository;
import com.turbo.tracker.repository.ProjectRepository;
import com.turbo.tracker.repository.TaskRepository;
import com.turbo.tracker.repository.TeamRepository;
import com.turbo.tracker.repository.UserRepository;

@SpringBootTest(properties = "turbo.seed.enabled=false")
@AutoConfigureMockMvc
class TrackerApiTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private CommentRepository comments;

	@Autowired
	private ProjectMemberRepository members;

	@Autowired
	private TaskRepository tasks;

	@Autowired
	private ProjectRepository projects;

	@Autowired
	private TeamRepository teams;

	@Autowired
	private UserRepository users;

	private String token;

	@BeforeEach
	void clean() throws Exception {
		comments.deleteAll();
		members.deleteAll();
		tasks.deleteAll();
		projects.deleteAll();
		teams.deleteAll();
		users.deleteAll();
		token = register("Ada", "ada@turbo.dev", "password");
	}

	@Test
	void createsTeamProjectAndTask() throws Exception {
		long teamId = idOf(json(post("/api/teams"), """
				{"name":"Platform","description":"Backend"}
				""")
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("Platform"))
				.andReturn());

		long projectId = idOf(json(post("/api/teams/" + teamId + "/projects"), """
				{"name":"Turbo API","description":"Team tracker"}
				""")
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.teamId").value(teamId))
				.andReturn());

		json(post("/api/projects/" + projectId + "/tasks"), """
				{"title":"Ship teams","priority":"HIGH"}
				""")
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("TODO"))
				.andExpect(jsonPath("$.projectId").value(projectId))
				.andExpect(jsonPath("$.priority").value("HIGH"));

		json(get("/api/teams/" + teamId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.projectCount").value(1));
	}

	@Test
	void rejectsBlankTeamName() throws Exception {
		json(post("/api/teams"), """
				{"name":"  "}
				""")
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", containsString("name")));
	}

	@Test
	void returns404ForMissingTeam() throws Exception {
		json(get("/api/teams/999"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Takım bulunamadı: 999"));
	}

	@Test
	void refusesToDeleteATeamThatStillHasProjects() throws Exception {
		long teamId = idOf(json(post("/api/teams"), """
				{"name":"Mobile"}
				""")
				.andExpect(status().isCreated())
				.andReturn());

		json(post("/api/teams/" + teamId + "/projects"), """
				{"name":"iOS"}
				""")
				.andExpect(status().isCreated());

		json(delete("/api/teams/" + teamId))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Bu takımın hâlâ projesi var"));
	}

	@Test
	void updatesTaskStatusThenDeletesIt() throws Exception {
		long teamId = idOf(json(post("/api/teams"), """
				{"name":"Web"}
				""")
				.andExpect(status().isCreated())
				.andReturn());
		long projectId = idOf(json(post("/api/teams/" + teamId + "/projects"), """
				{"name":"Site"}
				""")
				.andExpect(status().isCreated())
				.andReturn());
		long taskId = idOf(json(post("/api/projects/" + projectId + "/tasks"), """
				{"title":"Fix header","priority":"LOW"}
				""")
				.andExpect(status().isCreated())
				.andReturn());

		json(patch("/api/tasks/" + taskId), """
				{"status":"DONE"}
				""")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("DONE"))
				.andExpect(jsonPath("$.priority").value("LOW"));

		json(delete("/api/tasks/" + taskId)).andExpect(status().isNoContent());
		json(delete("/api/projects/" + projectId)).andExpect(status().isNoContent());
		json(delete("/api/teams/" + teamId)).andExpect(status().isNoContent());
		json(get("/api/tasks/" + taskId)).andExpect(status().isNotFound());
	}

	@Test
	void registersLogsInAndRejectsAnonymousCalls() throws Exception {
		mvc.perform(get("/api/teams")).andExpect(status().isUnauthorized());

		mvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email":"ada@turbo.dev","password":"password"}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isNotEmpty())
				.andExpect(jsonPath("$.user.role").value("ADMIN"));

		json(get("/api/auth/me"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("ada@turbo.dev"));

		mvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name":"Ada","email":"ada@turbo.dev","password":"password"}
						"""))
				.andExpect(status().isConflict());
	}

	@Test
	void managesMembersAssignsTasksCommentsAndSearches() throws Exception {
		String memberToken = register("Leo", "leo@turbo.dev", "password");
		long leoId = JsonPath.parse(mvc.perform(get("/api/auth/me")
				.header("Authorization", "Bearer " + memberToken))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString()).read("$.id", Number.class).longValue();

		long teamId = idOf(json(post("/api/teams"), """
				{"name":"Platform"}
				""").andReturn());
		long projectId = idOf(json(post("/api/teams/" + teamId + "/projects"), """
				{"name":"API"}
				""").andReturn());

		mvc.perform(post("/api/projects/" + projectId + "/tasks")
				.header("Authorization", "Bearer " + memberToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"title":"Blocked"}
						"""))
				.andExpect(status().isForbidden());

		json(post("/api/projects/" + projectId + "/members"), """
				{"userId":%d,"role":"MEMBER"}
				""".formatted(leoId))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.role").value("MEMBER"));

		long taskId = idOf(json(post("/api/projects/" + projectId + "/tasks"), """
				{"title":"Ship search","priority":"HIGH","assigneeId":%d}
				""".formatted(leoId))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.assigneeName").value("Leo"))
				.andReturn());

		json(post("/api/projects/" + projectId + "/tasks"), """
				{"title":"Write docs","priority":"LOW"}
				""").andExpect(status().isCreated());

		json(post("/api/tasks/" + taskId + "/comments"), """
				{"body":"Start with the query params"}
				""")
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.authorName").value("Ada"));

		json(get("/api/tasks/" + taskId + "/comments"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1));

		mvc.perform(get("/api/search/tasks")
				.header("Authorization", "Bearer " + token)
				.param("projectId", String.valueOf(projectId))
				.param("q", "ship")
				.param("priority", "HIGH")
				.param("assigneeId", String.valueOf(leoId))
				.param("sort", "priority")
				.param("page", "0")
				.param("size", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalElements").value(1))
				.andExpect(jsonPath("$.content[0].title").value("Ship search"))
				.andExpect(jsonPath("$.totalPages").value(1));
	}

	private String register(String name, String email, String password) throws Exception {
		MvcResult result = mvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name":"%s","email":"%s","password":"%s"}
						""".formatted(name, email, password)))
				.andExpect(status().isCreated())
				.andReturn();
		return JsonPath.parse(result.getResponse().getContentAsString()).read("$.token", String.class);
	}

	private ResultActions json(MockHttpServletRequestBuilder builder, String body) throws Exception {
		return mvc.perform(builder.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body));
	}

	private ResultActions json(MockHttpServletRequestBuilder builder) throws Exception {
		return mvc.perform(builder.header("Authorization", "Bearer " + token));
	}

	private long idOf(MvcResult result) throws Exception {
		return JsonPath.parse(result.getResponse().getContentAsString()).read("$.id", Number.class).longValue();
	}

}
