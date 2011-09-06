package cs113.calendar.util;

/**
 * Thrown if the incorrect concrete model type is passed to a model backend. For
 * example, this exception be thrown if a non-Serializable Appointment
 * implementation was passed to a SerializableBackend. To avoid this exception,
 * only use model objects created by a backend's factory methods when
 * interacting with that backend.
 * 
 * @author Michael Koval
 * @see cs113.calendar.model.Backend
 */
public class ModelMismatchException extends RuntimeException {
	private static final long serialVersionUID = -8259703878046557809L;

	public ModelMismatchException() {
		super("Illegal mixing of different model classes.");
	}
}
