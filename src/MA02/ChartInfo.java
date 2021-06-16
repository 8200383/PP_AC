package MA02;

import Quickchart.ChartType;
import edu.ma02.core.enumerations.Parameter;
import edu.ma02.core.interfaces.IStatistics;

public class ChartInfo {
    String name;
    IStatistics[] chartData;
    Parameter parameter;
    ChartType type;

    ChartInfo(String name, IStatistics[] chartData, Parameter parameter, ChartType type) {
        this.name = name;
        this.chartData = chartData;
        this.parameter = parameter;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public IStatistics[] getChartData() {
        return chartData;
    }

    public Parameter getParameter() {
        return parameter;
    }

    public ChartType getType() {
        return type;
    }
}