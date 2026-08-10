source /opt/concourse-java.sh
export TERM=xterm-256color
setup_symlinks

setup_gradle_mirror() {
	[[ -n "${ARTIFACTORY_VIRTUAL_REPO_URL:-}" ]] || return 0

	local init_dir="${GRADLE_USER_HOME:-$HOME/.gradle}/init.d"
	mkdir -p "$init_dir"
	cat >"$init_dir/artifactory-mirror.gradle" <<'EOF'
def mirrorUrl = System.getenv("ARTIFACTORY_VIRTUAL_REPO_URL")
if (mirrorUrl) {
	def addMirror = { repositories ->
		repositories.maven {
			url = mirrorUrl
			credentials {
				username = System.getenv("ARTIFACTORY_USERNAME")
				password = System.getenv("ARTIFACTORY_PASSWORD")
			}
		}
	}

	beforeSettings { settings ->
		addMirror(settings.pluginManagement.repositories)
		settings.dependencyResolutionManagement {
			repositories {
				addMirror(delegate)
			}
		}
	}
}
EOF
}
setup_gradle_mirror
