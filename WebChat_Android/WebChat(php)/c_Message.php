<?php
include_once("c_Tools.php");
	class Message
	{
		public $id;
        public $user_id;
        public $text;

		function __construct($user_id, $text, $id=0)
		{
			$this->id = $id;
			$this->user_id = $user_id;
            $this->text = $text;
		}

        function intoDb()
		{
			try{
				$pdo=Tools::connect();
				$ps=$pdo->prepare('insert into Messages (user_id, text) values(:user_id, :text)');
				$ar=(array)$this;
				array_shift($ar);
				$ps->execute($ar);
			}
			catch(PDOException $ex)
			{
				$err=$ex->getMessage();
				if(substr($err,0,strpos($err,":")) == 'SQLSTATE[23000]: Integrity constrait violation')
					return 1062;
				else
					echo $ex->getMessage();
			}
		}

		static function fromDb($id)
		{
			try
			{
				$user=null;
				$pdo=Tools::connect();
				$ps=$pdo->prepare('select * from Messages where id=?');
				$res=$ps->execute(array($id));
				$row=$res->fetch();
				$user=new User($row['user_id'], $row['text'], $row['id']);
				return $user;
			}
			catch(PDOException $ex)
			{
				$ex->getMessage();
				return false;
			}
		}

    }
?>