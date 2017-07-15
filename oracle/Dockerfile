FROM oracle11g-installed
MAINTAINER Mathieu GAILLARD

COPY sql /opt/ImageIndex/sql
COPY scripts /opt/ImageIndex/scripts
COPY java /opt/ImageIndex/java
COPY entrypoint.sh /opt/ImageIndex/entrypoint.sh
RUN chmod +x /opt/ImageIndex/entrypoint.sh

CMD ["/opt/ImageIndex/entrypoint.sh"]