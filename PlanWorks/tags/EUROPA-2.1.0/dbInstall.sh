#!/bin/sh

if [ $# != 8 ]; then
	echo "Usage: $1 <bindir> <basedir> <datadir> <log> <log_err> <sock> <tmpdir> <port>"
	exit
fi

if [ -z $PLANWORKS_HOME ]; then
echo "Error: PLANWORKS_HOME not defined."
fi

/bin/rm -f ${PLANWORKS_HOME?}/lib/mysql/data/mysql/*
/bin/rm -rf ${PLANWORKS_HOME?}/lib/mysql/data/PlanWorks

#utility database creation code taken from mysql_install_db.

# Initialize variables
c_d="" i_d=""
c_h="" i_h=""
c_u="" i_u=""
c_f="" i_f=""
c_t="" c_c=""
c_m=""
  echo "Preparing mysql database"
  c_m="CREATE DATABASE IF NOT EXISTS mysql;"
  echo "Preparing db table"

  # mysqld --bootstrap wants one command/line
  c_d="$c_d CREATE TABLE db ("
  c_d="$c_d   Host char(60) binary DEFAULT '' NOT NULL,"
  c_d="$c_d   Db char(64) binary DEFAULT '' NOT NULL,"
  c_d="$c_d   User char(16) binary DEFAULT '' NOT NULL,"
  c_d="$c_d   Select_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_d="$c_d   Insert_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_d="$c_d   Update_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_d="$c_d   Delete_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_d="$c_d   Create_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_d="$c_d   Drop_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_d="$c_d   Grant_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_d="$c_d   References_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_d="$c_d   Index_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_d="$c_d   Alter_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_d="$c_d   Create_tmp_table_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_d="$c_d   Lock_tables_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_d="$c_d PRIMARY KEY Host (Host,Db,User),"
  c_d="$c_d KEY User (User)"
  c_d="$c_d )"
  c_d="$c_d comment='Database privileges';"
  
  i_d="INSERT INTO db VALUES ('%','test','','Y','Y','Y','Y','Y','Y','N','Y','Y','Y','Y','Y');
  INSERT INTO db VALUES ('%','test\_%','','Y','Y','Y','Y','Y','Y','N','Y','Y','Y','Y','Y');"

  echo "Preparing host table"

  c_h="$c_h CREATE TABLE host ("
  c_h="$c_h  Host char(60) binary DEFAULT '' NOT NULL,"
  c_h="$c_h  Db char(64) binary DEFAULT '' NOT NULL,"
  c_h="$c_h  Select_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_h="$c_h  Insert_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_h="$c_h  Update_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_h="$c_h  Delete_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_h="$c_h  Create_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_h="$c_h  Drop_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_h="$c_h  Grant_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_h="$c_h  References_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_h="$c_h  Index_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_h="$c_h  Alter_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_h="$c_h  Create_tmp_table_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_h="$c_h  Lock_tables_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_h="$c_h  PRIMARY KEY Host (Host,Db)"
  c_h="$c_h )"
  c_h="$c_h comment='Host privileges;  Merged with database privileges';"

  echo "Preparing user table"

  c_u="$c_u CREATE TABLE user ("
  c_u="$c_u   Host char(60) binary DEFAULT '' NOT NULL,"
  c_u="$c_u   User char(16) binary DEFAULT '' NOT NULL,"
  c_u="$c_u   Password char(16) binary DEFAULT '' NOT NULL,"
  c_u="$c_u   Select_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_u="$c_u   Insert_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_u="$c_u   Update_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_u="$c_u   Delete_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_u="$c_u   Create_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_u="$c_u   Drop_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_u="$c_u   Reload_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_u="$c_u   Shutdown_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_u="$c_u   Process_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_u="$c_u   File_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_u="$c_u   Grant_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_u="$c_u   References_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_u="$c_u   Index_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_u="$c_u   Alter_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_u="$c_u   Show_db_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_u="$c_u   Super_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_u="$c_u   Create_tmp_table_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_u="$c_u   Lock_tables_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_u="$c_u   Execute_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_u="$c_u   Repl_slave_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_u="$c_u   Repl_client_priv enum('N','Y') DEFAULT 'N' NOT NULL,"
  c_u="$c_u   ssl_type enum('','ANY','X509', 'SPECIFIED') DEFAULT '' NOT NULL,"
  c_u="$c_u   ssl_cipher BLOB NOT NULL,"
  c_u="$c_u   x509_issuer BLOB NOT NULL,"
  c_u="$c_u   x509_subject BLOB NOT NULL,"
  c_u="$c_u   max_questions int(11) unsigned DEFAULT 0  NOT NULL,"
  c_u="$c_u   max_updates int(11) unsigned DEFAULT 0  NOT NULL,"
  c_u="$c_u   max_connections int(11) unsigned DEFAULT 0  NOT NULL,"
  c_u="$c_u   PRIMARY KEY Host (Host,User)"
  c_u="$c_u )"
  c_u="$c_u comment='Users and global privileges';"

  i_u="INSERT INTO user VALUES ('localhost','root','','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','','','','',0,0,0);
  INSERT INTO user VALUES ('$hostname','root','','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','','','','',0,0,0);
  
  REPLACE INTO user VALUES ('localhost','root','','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','','','','',0,0,0);
  REPLACE INTO user VALUES ('$hostname','root','','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','Y','','','','',0,0,0);
  
  INSERT INTO user (host,user) values ('localhost','');
  INSERT INTO user (host,user) values ('$hostname','');"

  echo "Preparing func table"

  c_f="$c_f CREATE TABLE func ("
  c_f="$c_f   name char(64) binary DEFAULT '' NOT NULL,"
  c_f="$c_f   ret tinyint(1) DEFAULT '0' NOT NULL,"
  c_f="$c_f   dl char(128) DEFAULT '' NOT NULL,"
  c_f="$c_f   type enum ('function','aggregate') NOT NULL,"
  c_f="$c_f   PRIMARY KEY (name)"
  c_f="$c_f )"
  c_f="$c_f   comment='User defined functions';"

  echo "Preparing tables_priv table"

  c_t="$c_t CREATE TABLE tables_priv ("
  c_t="$c_t   Host char(60) binary DEFAULT '' NOT NULL,"
  c_t="$c_t   Db char(64) binary DEFAULT '' NOT NULL,"
  c_t="$c_t   User char(16) binary DEFAULT '' NOT NULL,"
  c_t="$c_t   Table_name char(60) binary DEFAULT '' NOT NULL,"
  c_t="$c_t   Grantor char(77) DEFAULT '' NOT NULL,"
  c_t="$c_t   Timestamp timestamp(14),"
  c_t="$c_t   Table_priv set('Select','Insert','Update','Delete','Create','Drop','Grant','References','Index','Alter') DEFAULT '' NOT NULL,"
  c_t="$c_t   Column_priv set('Select','Insert','Update','References') DEFAULT '' NOT NULL,"
  c_t="$c_t   PRIMARY KEY (Host,Db,User,Table_name),"
  c_t="$c_t   KEY Grantor (Grantor)"
  c_t="$c_t )"
  c_t="$c_t   comment='Table privileges';"

  echo "Preparing columns_priv table"

  c_c="$c_c CREATE TABLE columns_priv ("
  c_c="$c_c   Host char(60) binary DEFAULT '' NOT NULL,"
  c_c="$c_c   Db char(64) binary DEFAULT '' NOT NULL,"
  c_c="$c_c   User char(16) binary DEFAULT '' NOT NULL,"
  c_c="$c_c   Table_name char(64) binary DEFAULT '' NOT NULL,"
  c_c="$c_c   Column_name char(64) binary DEFAULT '' NOT NULL,"
  c_c="$c_c   Timestamp timestamp(14),"
  c_c="$c_c   Column_priv set('Select','Insert','Update','References') DEFAULT '' NOT NULL,"
  c_c="$c_c   PRIMARY KEY (Host,Db,User,Table_name,Column_name)"
  c_c="$c_c )"
  c_c="$c_c   comment='Column privileges';"

echo "Installing all prepared tables"
echo "$1 $2 $3"
eval "$1/mysqld --bootstrap --skip-grant-tables --basedir=$2 --datadir=$3 --port=$8 --skip-innodb --skip-bdb " << END_OF_DATA
use mysql;
$c_m

$c_d
$i_d

$c_h
$i_h

$c_u
$i_u

$c_f
$i_f

$c_t
$c_c
END_OF_DATA

echo "Starting database..."
eval "$1/mysqld --basedir=$2 --skip-bdb --datadir=$3 --log=$4 --log-error=$5 --skip-symlink --socket=$6 --tmpdir=$7 --port=$8 &"
sleep 1
echo "Creating PlanWorks database..."
eval "$1/mysql --user=root --socket=$6 --port=$8 --execute=\"CREATE DATABASE IF NOT EXISTS PlanWorks\""
sleep 1
echo "Creating PlanWorks tables..."
eval "$1/mysql --user=root --database=PlanWorks --socket=$6 --port=$8 < PlanWorksTables"
sleep 1
echo "Shutting down database..."
eval "$1/mysqladmin --user=root --socket=$6 --port=$8 shutdown"
