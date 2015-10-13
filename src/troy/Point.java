package troy;

/**
 * A highly lightweight implementation of an integer point.
 * 
 * @author Sebastian Troy
 */
public class Point {
	final int x, y;

	Point(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Point)) {
			return false;
		}
		else {
			Point other = (Point) obj;
			return other.x == x && other.y == y;
		}
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 71 * hash + x;
		hash = 71 * hash + y;
		return hash;
	}
	
	@Override
	public String toString() {
		return new StringBuilder().append("(").append(x).append(", ").append(y).append(")").toString();
	}
}
