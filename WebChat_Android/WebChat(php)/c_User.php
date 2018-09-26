<?php
include_once("c_Tools.php");
	class User
	{
		public $id;
		public $login;
		public $pass;
        public $nick;
        public $status;

		function __construct($l, $password, $name, $status=0, $id=0)
		{
			$this->id = $id;
			$this->login = $l;
			$this->pass = $password;
			$this->nick = $name;
			$this->status = $status;
		}

        function intoDb()
		{
			try{
				$pdo=Tools::connect();
				$ps=$pdo->prepare('insert into Users (login, pass, nick, status, lastRequest) values(:login, :pass, :nick, :status, NOW())');
				$ar=(array)$this;
				array_shift($ar);
				$ps->execute($ar);
				echo "<p name='db_insert'>success</p>";
			}
			catch(PDOException $ex)
			{
				$err=$ex->getMessage();
				if(substr($err,0,strpos($err,":")) == 'SQLSTATE[23000]: Integrity constrait violation')
					return 1062;
				else
					echo "<p name='db_insert_nick_exists'>".$ex->getMessage()."</p>";
			}
		}

		static function fromDb($id)
		{
			try{
				$user=null;
				$pdo=Tools::connect();
				$ps=$pdo->prepare('select * from Users where id=?');
				$res=$ps->execute(array($id));
				$row=$res->fetch();
				$user=new User($row['login'], $row['pass'], $row['nick'], $row['id']);
				return $user;
			}
			catch(PDOException $ex)
			{
				$ex->getMessage();
				return false;
			}
		}

		static function changeStatus($id, $st)
		{
			try
			{
				$pdo=Tools::connect();
				$ps=$pdo->prepare('update Users set status = ? where id = ?');
				$ps->execute(array($st, $id));
			}
			catch(PDOException $ex)
			{
				$ex->getMessage();
				return false;
			}
		}

		static function updateTime($id, $st)
		{
			try
			{
				$pdo=Tools::connect();
				$ps=$pdo->prepare('update Users set status = ?, lastRequest = NOW() where id = ?');
				$ps->execute(array($st, $id));
			}
			catch(PDOException $ex)
			{
				$ex->getMessage();
				return false;
			}
		}

		static function setOffline()
		{
			try
			{
				$pdo=Tools::connect();
				$ps=$pdo->prepare('update Users set status = 0 where status = 1 and 15 < TIMEDIFF(NOW(), lastRequest)');
				$ps->execute();
			}
			catch(PDOException $ex)
			{
				$ex->getMessage();
				return false;
			}
		}
    }
?>