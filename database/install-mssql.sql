create table swipe_record
(
    id VARCHAR(36)
        constraint swipe_record_pk
            primary key nonclustered,
    ts BIGINT not null,
    swipe VARCHAR(255) not null,
    user_id VARCHAR(255),
    user_first_name VARCHAR(255),
    user_last_name VARCHAR(255),
    user_email VARCHAR(255),
    error_msg VARCHAR(255),
    cresco_region VARCHAR(255),
    cresco_agent VARCHAR(255),
    cresco_plugin VARCHAR(255)
)
go

