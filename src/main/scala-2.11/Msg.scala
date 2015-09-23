sealed trait Msg
case class Connect() extends Msg
case class Rumor() extends Msg
case class Term() extends Msg
case class Start() extends Msg

