FROM quay.io/strimzi/kafka:0.51.0-kafka-4.2.0

USER root:root

# Install unzip (not present in base image)
RUN microdnf install -y unzip && microdnf clean all

# Copy zip into image
COPY ./plugins/debezium-connector-mysql-3.5.0.Final-plugin.zip /tmp/

# Unzip plugin to correct directory, set permissions and clean up
RUN unzip /tmp/debezium-connector-mysql-3.5.0.Final-plugin.zip -d /opt/kafka/plugins/debezium-connector-mysql && \
    rm /tmp/debezium-connector-mysql-3.5.0.Final-plugin.zip && \
    chown -R 1001:0 /opt/kafka/plugins

USER 1001
