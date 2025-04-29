# Sets up the GitPod shell using the Java 21 workspace which then
# also sets SDKMAN for GitPod as finalization.

FROM gitpod/workspace-java-21
USER gitpod

SHELL ["/bin/bash", "-c"]

RUN source ~/.sdkman/bin/sdkman-init.sh && sdk install java 21.0.7-tem && sdk use java 21.0.7-tem
