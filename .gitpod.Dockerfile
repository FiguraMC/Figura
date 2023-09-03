FROM gitpod/workspace-java-17
USER gitpod

SHELL ["/bin/bash", "-c"]

RUN source ~/.sdkman/bin/sdkman-init.sh && \
    sdk install java 17.0.8.1-tem && \
    sdk use java 17.0.8.1-tem