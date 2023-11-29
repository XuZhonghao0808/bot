# java17运行环境
FROM openjdk:17-jdk
# 作者名称
MAINTAINER xzh

COPY Fonts/ /usr/share/fonts/chinese/

ENV LANGUAGE zh_CN.UTF-8
ENV LC_ALl zh_CN.UTF-8

RUN fc-cache -f -v
# 切换工作目录
WORKDIR /bot

# 添加rundemo.jar文件到docker环境内
ADD bot-0.0.1-SNAPSHOT.jar /bot/bot-0.0.1-SNAPSHOT.jar

# 暴露端口8080
EXPOSE 8080
# 运行命令
ENTRYPOINT ["java", "-server", "-Xms1024m", "-Xmx5120m", "-jar", "/bot/bot-0.0.1-SNAPSHOT.jar"]