CREATE KEYSPACE user_connector WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };

CREATE TABLE user_connector.connection_request
(
    recipient_id  uuid,
    requester_id  uuid,
    creation_time timestamp,
    PRIMARY KEY ((recipient_id), creation_time)
);
