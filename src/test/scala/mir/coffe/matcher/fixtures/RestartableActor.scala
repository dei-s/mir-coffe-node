package mir.coffe.matcher.fixtures

import akka.persistence.PersistentActor

import mir.coffe.matcher.fixtures.RestartableActor.{RestartActor, RestartActorException}

trait RestartableActor extends PersistentActor {

  abstract override def receiveCommand: PartialFunction[Any, Unit] = super.receiveCommand orElse {
    case RestartActor => throw RestartActorException
  }
}

object RestartableActor {
  case object RestartActor

  private object RestartActorException extends Exception("Planned restart")
}
