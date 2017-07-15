FROM ubuntu:latest
MAINTAINER Mathieu GAILLARD

RUN apt update \
&& apt install -y curl libaio1 build-essential python

COPY instantclient_12_1 /opt/oracle/instantclient_12_1

RUN ln -s /opt/oracle/instantclient_12_1/libclntsh.so.12.1 /opt/oracle/instantclient_12_1/libclntsh.so \
&& ln -s /opt/oracle/instantclient_12_1/libocci.so.12.1 /opt/oracle/instantclient_12_1/libocci.so

RUN curl -sL https://deb.nodesource.com/setup_6.x | bash - \
&& apt install -y nodejs \
&& apt clean

RUN npm install -g @angular/cli

COPY entrypoint.sh /opt/entrypoint.sh
RUN chmod +x /opt/entrypoint.sh

COPY app /opt/app

WORKDIR /opt/app

RUN export LD_LIBRARY_PATH=/opt/oracle/instantclient_12_1:$LD_LIBRARY_PATH \
&& export PATH=/opt/oracle/instantclient_12_1:$PATH \
&& export OCI_LIB_DIR=/opt/oracle/instantclient_12_1 \
&& export OCI_INC_DIR=/opt/oracle/instantclient_12_1/sdk/include \
&& npm install \
&& ng build

CMD ["/opt/entrypoint.sh"]