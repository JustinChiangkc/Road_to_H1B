///////////////////////////  hypothesis testing /////////////////////////// 

/*
Objective -> test the Credibility of linear equation [salary, acceptance rate]
Method -> test the slope of linear equation is siginificant or not
linear equation -> Yi=α+β(xi−x¯)+ϵi
    Y : H1-B acceptance rate
    X : median of salary [per company and state]
    β : slope


a (1−α)100% confidence interval for β [two-tailed t test]:
    null hypothesis -> H0 : β=β0  [β0 : the slope we got]
    alternative hypotheses -> HA : β≠β0, HA:β<β0, HA:β>β0  
    test statistic -> t = ^β−β0 / [(MSE/∑(xi−x)^2)**2]
    α -> 0.05
*/


import org.apache.spark.ml.regression.LinearRegressionSummary


def isSignificant(Double: slope) : Boolean = {

    // pvalues and tvalues
    val summary = lrModel.summary
    val pvalue = summary.pValues(0)
    val (coefficient, intercept) = (summary.tValues(0), summary.tValues(1))
    val MSE = summary.meanSquaredError
    
    println(s"Coefficients: ${coefficient} Intercept: ${intercept}")
    println(s"MSE: ${MSE}")
    println(s"p value: ${pvalue}")

    // judgement
    if (pvalue < 0.05) {
        val isSignificant: Boolean = true;
    }
    else {
        val isSignificant: Boolean = false;
    }

    println(s"isSignificant : ${isSignificant}")
    
    return isSignificant
}
