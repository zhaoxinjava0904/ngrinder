package org.ngrinder.script.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.validator.routines.UrlValidator;
import org.ngrinder.common.exception.InvalidGitHubConfigurationException;

import java.io.IOException;

import static org.apache.commons.validator.routines.UrlValidator.getInstance;

@Getter
@Setter
@Builder
@ToString
@JsonDeserialize(using = GitHubConfig.GitHubConfigDeserializer.class)
public class GitHubConfig {
	private String name;
	private String owner;
	private String repo;
	private String accessToken;
	private String branch;
	private String baseUrl;
	private String revision;
	private String scriptRoot;

	public static class GitHubConfigDeserializer extends JsonDeserializer<GitHubConfig> {

		@Override
		public GitHubConfig deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
			ObjectCodec objectCodec = jsonParser.getCodec();
			JsonNode jsonNode = objectCodec.readTree(jsonParser);
			UrlValidator urlValidator = getInstance();

			String baseUrl = defaultIfNull(jsonNode.get("base-url"), "");
			if (!baseUrl.isEmpty() && !urlValidator.isValid(baseUrl)) {
				throw new InvalidGitHubConfigurationException("Field 'base-url' is invalid.\nPlease check your .gitconfig.yml");
			}

			try {
				return GitHubConfig.builder()
					.name(jsonNode.get("name").asText())
					.owner(jsonNode.get("owner").asText())
					.repo(jsonNode.get("repo").asText())
					.accessToken(jsonNode.get("access-token").asText())
					.branch(defaultIfNull(jsonNode.get("branch"), ""))
					.baseUrl(baseUrl)
					.revision(defaultIfNull(jsonNode.get("revision"), "-1"))
					.scriptRoot(defaultIfNull(jsonNode.get("script-root"), ""))
					.build();
			} catch (RuntimeException e) {
				throw new InvalidGitHubConfigurationException("Some of required fields(name, owner, repo, access-token) are missing.\n" +
					"Please check your .gitconfig.yml", e);
			}
		}

		private String defaultIfNull(JsonNode jsonNode, String defaultValue) {
			return jsonNode == null ? defaultValue : jsonNode.asText(defaultValue);
		}
	}
}
