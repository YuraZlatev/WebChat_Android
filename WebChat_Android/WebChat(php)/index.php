<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>WebChat</title>
</head>
<body>
    
<?php
    include_once("c_Tools.php");
    include_once("c_User.php");
    include_once("c_Message.php");

    echo "Server 'WebChat' is running<br/>";

    $action = "";
    if (isset($_POST["action"])) { 
        $action = $_POST['action'];
        User::setOffline();
    }


    if($action == "new_message")
    {
        $msg = $_POST['message'];
        $user_id = $_POST['user_id'];
        $message = new Message($user_id, $msg);
        $message->intoDb();
        $action = "update_messages";
    }


    if($action == "update_messages")
    {
        try
        {
            $lastId = $_POST['last_message'];
            $pdo = Tools::connect();
            $ps = $pdo->query('select m.id, m.text, u.nick from Messages m, Users u where m.user_id = u.id and m.id > '.$lastId);
            $ps->execute();
            $mgs = '<p name="all_messages">';
            while($row=$ps->fetch())
            {
                $mgs .= "|".$row['id']."&".$row['text']."&".$row['nick']."|";
            }
            $mgs .= '</p>';

            $user_id = $_POST['user_id'];
            User::updateTime($user_id, 1);
            print_r(json_encode($mgs));
        }
        catch(PDOException $ex)
        {
            echo "<p>failed request</p>";
        }
    }


    if($action == "exit_user")
    {
        $id = $_POST['user_id'];
        User::updateTime($id, 0);
    }

     
    if($action == "insert_user")
    {
        $login = $_POST['login'];
        $pass = $_POST['pass'];
        $name = $_POST['nick'];
        $user = new User($login, $pass, $name);
        $user->intoDB();
    }

    if($action == "select_all_users")
    {
        try{
            $pdo = Tools::connect();
            $ps = $pdo->query('select nick, status from Users');
            $ps->execute();
            $users = '';
            while($row=$ps->fetch())
            {
                $users .= "|".$row['nick']."&".$row['status']."|";
            }

            $id = $_POST['user_id'];
            User::updateTime($id, 1);
            print_r(json_encode($users));
        }
        catch(PDOException $ex)
        {
            echo "<p>failed request</p>";
        }
    }

    if($action == "select_user")
    {
        try{
            $name = $_POST['login'];
            $password = $_POST['pass'];
            $pdo = Tools::connect();
            $ps = $pdo->prepare('select id, login, nick from Users where login = ? and pass = ?');
            $ps->execute(array($name, $password));
            $row = $ps->fetch();

            $id = $row['id'];
            $login = $row['login'];
            $nick = $row['nick'];
            if($id == "")
                echo "<p name=db_select_failed></p>";
            else
            {
                User::changeStatus($id, 1);
                echo '<p name="db_select_success">&&'.$id."|".$login."|".$nick.'&&</p>';
            }
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

?>

</body>
</html>



