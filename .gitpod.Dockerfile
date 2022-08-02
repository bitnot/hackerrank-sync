FROM gitpod/workspace-full

RUN sudo sh -c '(echo "#!/usr/bin/env sh" && curl -L https://github.com/lihaoyi/Ammonite/releases/download/2.5.4/3.1-2.5.4) > /usr/local/bin/amm && chmod +x /usr/local/bin/amm'

RUN brew install scala coursier/formulas/coursier sbt

# RUN brew install --HEAD scalaenv && scalaenv install scala-3.1.3 && scalaenv global scala-3.1.3
