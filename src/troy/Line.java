package troy;

import java.util.function.Consumer;

/**
 * NOTE: This class is heavily optimised for simple and specialised usage. It only works with lines whose normalised vectors are also
 * representable by integers, i.e. vertical, horizontal and the two diagonals halfway between. This is not enforced however it will lead to
 * infinite recursion in {@link #intersectsPoint(Point)}.
 * 
 * @author Sebastian Troy
 */
public class Line {
	public enum Direction {
		Horizontal, Vertical, DiagonalUp, DiagonalDown
	}

	final Point start, end;
	final int length;
	private final Point normalisedVector;

	Line(int startX, int startY, int endX, int endY) {
		this(new Point(startX, startY), new Point(endX, endY));
	}

	Line(Point start, Point end) {
		this.start = start;
		this.end = end;
		this.normalisedVector = new Point(Integer.signum(end.x - start.x), Integer.signum(end.y - start.y));
		// Diagonal up lines suffer from length miscalculation due to it containing the only negative gradient
		this.length = Math.max(Math.abs(end.x - start.x), Math.abs(end.y - start.y)) + 1;
	}
	
	static final Line getReversedLine(Line lineToReverse) {
		return new Line(lineToReverse.end, lineToReverse.start);
	}

	Point getNormalisedVector() {
		return normalisedVector;
	}

	Line getSubLine(int firstPointIndex, int secondPointIndex) {
		assert (firstPointIndex < secondPointIndex);
		assert (firstPointIndex >= 0);
		assert (secondPointIndex < length);

		Point startPoint = new Point(start.x + (firstPointIndex * normalisedVector.x), start.y + (firstPointIndex * normalisedVector.y));
		Point endPoint = new Point(start.x + (secondPointIndex * normalisedVector.x), start.y + (secondPointIndex * normalisedVector.y));

		return new Line(startPoint, endPoint);
	}

	void forEachPoint(Consumer<Point> action) {
		assert (action != null);

		int x = start.x;
		int y = start.y;
		for (int index = 0; index < length; index++) {
			action.accept(new Point(x, y));
			x += normalisedVector.x;
			y += normalisedVector.y;
		}
	}
	
	void forEachSubline(Consumer<Line> action) {
		assert(action != null);
		
		for (int startIndex = 0; startIndex < length; startIndex++) {
			for (int endIndex = startIndex; endIndex < length; endIndex++) {
				
			}
		}
	}

	/**
	 * @param point {@link Point} to check.
	 * @return <code>true</code> if a point would be under this line if it were drawn 1:1 on screen.
	 */
	boolean intersectsPoint(Point point) {
		boolean intersects = false;
		
		int x = start.x;
		int y = start.y;
		for (int index = 0; index < length; index++) {
			if (point.x == x && point.y == y) {
				return true;
			}
			x += normalisedVector.x;
			y += normalisedVector.y;
		}
		
		return intersects;
	}

	/**
	 * @param other {@link Line}to check.
	 * @return <code>true</code> if the specified line is the same as this, or a subsection of it.
	 */
	boolean containsLine(Line other) {
		if (this.equals(other)) {
			return true;
		}

		return intersectsPoint(other.start) && intersectsPoint(other.end);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		assert (obj instanceof Line);

		Line other = (Line) obj;
		return other.start.equals(start) && other.end.equals(end);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int hash = start.hashCode();
		hash = 71 * hash + end.hashCode();
		return hash;
	}

	@Override
	public String toString() {
		return new StringBuilder().append("Line [").append(start.toString()).append(", ").append(end.toString()).append("] nv=").append(normalisedVector.toString()).append("length=").append(length).toString();
	}
}
