# Sets up the GitPod shell using the Java 17 workspace which then
# also sets SDKMAN for GitPod as finalization.

FROM gitpod/workspace-java-17
USER gitpod

SHELL ["/bin/bash", "-c"]

RUN source ~/.sdkman/bin/sdkman-init.sh && sdk install java 17.0.10-tem && sdk use java 17.0.10-tem
