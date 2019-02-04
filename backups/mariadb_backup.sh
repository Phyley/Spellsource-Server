#!/usr/bin/env bash

# FROM https://github.com/iainmckay/docker-mysql-backup/blob/master/start.sh

EXCLUDE_OPT=
PASS_OPT=

for i in "$@"; do
    case ${i} in
        --exclude=*)
        EXCLUDE_OPT="${i#*=}"
        shift
        ;;
        *)
            # unknown option
        ;;
    esac
done

if [[ -n ${MYSQL_PASSWORD} ]]; then
    PASS_OPT="--password=${MYSQL_PASSWORD}"
fi

if [[ -n ${EXCLUDE_OPT} ]]; then
    EXCLUDE_OPT="| grep -Ev (${EXCLUDE_OPT//,/|})"
fi

if [[ "$1" == "backup" ]]; then
    if [[ -n "$2" ]]; then
        databases=$2
    else
        databases=`mysql --user=${MYSQL_USER} --host=${MYSQL_HOST} --port=${MYSQL_PORT} ${PASS_OPT} -e "SHOW DATABASES;" | grep -Ev "(Database|information_schema|performance_schema)" ${EXCLUDE_OPT}`
    fi

    for db in ${databases}; do
        db_filename=${db}_$(date '+%Y-%m-%d_%H_%M_%S')
        echo "dumping $db"

        mysqldump --force --opt --host=${MYSQL_HOST} --port=${MYSQL_PORT} --user=${MYSQL_USER} --databases ${db} ${PASS_OPT} | gzip > "/tmp/$db_filename.gz"

        if [ $? == 0 ]; then
            aws s3 cp /tmp/${db_filename}.gz s3://${S3_BUCKET_NAME}/${S3_BUCKET_PATH}/${db_filename}.gz

            if [ $? == 0 ]; then
                rm /tmp/${db}.gz
            else
                >&2 echo "couldn't transfer $db.gz to S3"
            fi
        else
            >&2 echo "couldn't dump $db"
        fi
    done
else
    >&2 echo "Only supports backup"
    exit 64
fi