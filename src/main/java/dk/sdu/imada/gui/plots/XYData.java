package dk.sdu.imada.gui.plots;

import org.jfree.data.DomainInfo;
import org.jfree.data.Range;
import org.jfree.data.RangeInfo;
import org.jfree.data.xy.AbstractXYZDataset;
import org.jfree.data.xy.XYDataset;

@SuppressWarnings("serial")
public class XYData extends AbstractXYZDataset implements 
XYDataset, DomainInfo, RangeInfo {

	/**
	 * 
	 */
	double [][]xValues;
	double [][]yValues;

	int seriesCount;
	int itemCount;

	double minX = Double.POSITIVE_INFINITY;
	double maxX = Double.NEGATIVE_INFINITY;
	double minY = Double.POSITIVE_INFINITY;
	double maxY = Double.NEGATIVE_INFINITY;

	public XYData(double[] dataX, double[] dataY) {

		this.seriesCount = 1;
		this.itemCount = dataX.length;
		
		xValues = new double[seriesCount][itemCount];
		yValues = new double[seriesCount][itemCount];

		for (int i = 0; i < itemCount; i++) {

			double x = dataX[i];
			if (x < minX) {
				minX = x;
			}
			if (x > maxX) {
				maxX = x;
			}

			double y = dataY[i];
			if (y < minY) {
				minY = y;
			}
			if (y > maxY) {
				maxY = y;
			}

			xValues[0][i] = x;
			yValues[0][i] = y;
		}
	}
	
	public XYData(double[] dataX, double[] dataY, double maxLogValue) {

		this.seriesCount = 1;
		this.itemCount = dataX.length;
		
		xValues = new double[seriesCount][itemCount];
		yValues = new double[seriesCount][itemCount];

		for (int i = 0; i < itemCount; i++) {

			double x = dataX[i];
			if (Double.isInfinite(x)) {
				x = maxLogValue;
			}
			
			if (x < minX) {
				minX = x;
			}
			if (x > maxX) {
				maxX = x;
			}

			double y = dataY[i];
			
			if (Double.isInfinite(y)) {
				y = maxLogValue;
			}
			
			if (y < minY) {
				minY = y;
			}
			if (y > maxY) {
				maxY = y;
			}

			xValues[0][i] = x;
			yValues[0][i] = y;
		}
	}

	// AbstractXYZDataset implementation
	public Number getZ(int series, int item) {
		return null;
	}

	public int getItemCount(int series) {
		return itemCount;
	}

	public Number getX(int series, int item) {
		return this.xValues[series][item];
	}

	public Number getY(int series, int item) {
		return this.yValues[series][item];
	}

	@Override
	public int getSeriesCount() {
		return seriesCount;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Comparable getSeriesKey(int series) {
		return "Sample " + series;
	}
	// ............

	// . Domain Implementation 
	public double getDomainLowerBound(boolean includeInterval) {
		return minX;
	}

	public double getDomainUpperBound(boolean includeInterval) {
		return maxX;
	}

	public Range getDomainBounds(boolean includeInterval) {
		return new Range(minX, maxX);
	}

	//....... Range Info implementation
	public double getRangeLowerBound(boolean includeInterval) {
		return minY;
	}

	public double getRangeUpperBound(boolean includeInterval) {
		return maxY;
	}

	public Range getRangeBounds(boolean includeInterval) {
		return new Range(minY, maxY);
	}
}
