#!/bin/sh

eval "$1/mysqld --basedir=$2 --skip-bdb --bind-address=127.0.0.1 --datadir=$3 --log=$4 --log-error=$5 --skip-symlink --socket=$6 --tmpdir=$7 &"

eval "$1/mysql --user=PlanWorksUser --password=PlanWorksUser --host=127.0.0.1 --socket=$6 < cleandb"

eval "$1/mysqladmin --user=PlanWorksUser --password=PlanWorksUser --host=127.0.0.1 --socket=$6 shutdown"
