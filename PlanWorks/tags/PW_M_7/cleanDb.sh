#!/bin/sh

eval "$1/mysqld --basedir=$2 --skip-bdb --datadir=$3 --log=$4 --log-error=$5 --skip-symlink --socket=$6 --tmpdir=$7 &"

sleep 1

eval "$1/mysql --user=root --socket=$6 < cleandb"

eval "$1/mysqladmin --user=root --socket=$6 shutdown"
