package se.citerus.dddsample.infrastructure.messaging.jms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jms.core.JmsOperations;
import org.springframework.jms.core.MessageCreator;
import se.citerus.dddsample.application.ApplicationEvents;
import se.citerus.dddsample.domain.model.cargo.Cargo;
import se.citerus.dddsample.domain.model.handling.HandlingEvent;
import se.citerus.dddsample.interfaces.handling.HandlingEventRegistrationAttempt;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * JMS based implementation.
 */
public final class JmsApplicationEventsImpl implements ApplicationEvents {

  private final JmsOperations jmsOperations;
  private final Destination cargoHandledQueue;
  private final Destination misdirectedCargoQueue;
  private final Destination deliveredCargoQueue;
  private final Destination rejectedRegistrationAttemptsQueue;
  private final Destination handlingEventQueue;

  private static final Log logger = LogFactory.getLog(JmsApplicationEventsImpl.class);

  public JmsApplicationEventsImpl(JmsOperations jmsOperations, Destination cargoHandledQueue,
                                  Destination misdirectedCargoQueue, Destination deliveredCargoQueue,
                                  Destination rejectedRegistrationAttemptsQueue, Destination handlingEventQueue) {
    this.jmsOperations = jmsOperations;
    this.cargoHandledQueue = cargoHandledQueue;
    this.misdirectedCargoQueue = misdirectedCargoQueue;
    this.deliveredCargoQueue = deliveredCargoQueue;
    this.rejectedRegistrationAttemptsQueue = rejectedRegistrationAttemptsQueue;
    this.handlingEventQueue = handlingEventQueue;
  }

  @Override
  public void cargoWasHandled(final HandlingEvent event) {
    final Cargo cargo = event.cargo();
    logger.info("Cargo was handled " + cargo);
    jmsOperations.send(cargoHandledQueue, new MessageCreator() {
      public Message createMessage(final Session session) throws JMSException {
        return session.createTextMessage(cargo.trackingId().idString());
      }
    });
  }

  @Override
  public void cargoWasMisdirected(final Cargo cargo) {
    logger.info("Cargo was misdirected " + cargo);
    jmsOperations.send(misdirectedCargoQueue, new MessageCreator() {
      public Message createMessage(Session session) throws JMSException {
        return session.createTextMessage(cargo.trackingId().idString());
      }
    });
  }

  @Override
  public void cargoHasArrived(final Cargo cargo) {
    logger.info("Cargo has arrived " + cargo);
    jmsOperations.send(deliveredCargoQueue, new MessageCreator() {
      public Message createMessage(Session session) throws JMSException {
        return session.createTextMessage(cargo.trackingId().idString());
      }
    });
  }

  @Override
  public void receivedHandlingEventRegistrationAttempt(final HandlingEventRegistrationAttempt attempt) {
    logger.info("Received handling event registration attempt " + attempt);
    jmsOperations.send(handlingEventQueue, new MessageCreator() {
      public Message createMessage(Session session) throws JMSException {
        return session.createObjectMessage(attempt);
      }
    });
  }

}
