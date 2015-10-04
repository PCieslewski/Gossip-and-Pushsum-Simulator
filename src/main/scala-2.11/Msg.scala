import akka.actor.ActorRef

//This file contains all of the message definitions.
sealed trait Msg
case class Connect() extends Msg
case class Rumor() extends Msg
case class Term() extends Msg
case class Start() extends Msg
case class AddNeighbor(neighbor: ActorRef) extends Msg
case class PushMsg(sMsg: Double, wMsg: Double) extends Msg
case class Ready() extends Msg
case class StartSimulation() extends Msg
case class GotMsg() extends Msg
case class Reply() extends Msg
case class Heartbeat() extends Msg
case class CheckHeartbeat() extends Msg
