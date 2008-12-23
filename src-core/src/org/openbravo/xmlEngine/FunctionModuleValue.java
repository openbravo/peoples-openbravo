/*
 ************************************************************************************
 * Copyright (C) 2001-2006 Openbravo S.L.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */
package org.openbravo.xmlEngine;

import org.apache.log4j.Logger;

class FunctionModuleValue extends FunctionEvaluationValue {

    static Logger log4jFunctionModuleValue = Logger
            .getLogger(FunctionModuleValue.class);

    public FunctionModuleValue(FunctionTemplate functionTemplate,
            XmlDocument xmlDocument) {
        super(functionTemplate, xmlDocument);
    }

    public String print() {
        log4jFunctionModuleValue.debug("Arg2: " + arg2Value.printSimple());
        log4jFunctionModuleValue.debug("Arg1: " + arg1Value.printSimple());

        if (arg1Value.print().equals(XmlEngine.strTextDividedByZero)
                || arg2Value.print().equals(XmlEngine.strTextDividedByZero)) {
            return XmlEngine.strTextDividedByZero;
        } else {
            double division = Double.valueOf(arg1Value.printSimple())
                    .doubleValue()
                    / Double.valueOf(arg2Value.printSimple()).doubleValue();
            if (Double.isInfinite(division) || Double.isNaN(division)) {
                return XmlEngine.strTextDividedByZero;
            } else {
                return functionTemplate.printFormatOutput(Double.valueOf(
                        arg1Value.printSimple()).doubleValue()
                        - Double.valueOf(arg2Value.printSimple()).doubleValue()
                        * Math.floor(division));
            }
        }
    }

    public String printSimple() {
        log4jFunctionModuleValue.debug("Arg2: " + arg2Value.printSimple());
        log4jFunctionModuleValue.debug("Arg1: " + arg1Value.printSimple());

        if (arg1Value.print().equals(XmlEngine.strTextDividedByZero)
                || arg2Value.print().equals(XmlEngine.strTextDividedByZero)) {
            return XmlEngine.strTextDividedByZero;
        } else {
            double division = Double.valueOf(arg1Value.printSimple())
                    .doubleValue()
                    / Double.valueOf(arg2Value.printSimple()).doubleValue();
            if (Double.isInfinite(division) || Double.isNaN(division)) {
                return XmlEngine.strTextDividedByZero;
            } else {
                return functionTemplate.printFormatSimple(Double.valueOf(
                        arg1Value.printSimple()).doubleValue()
                        - Double.valueOf(arg2Value.printSimple()).doubleValue()
                        * Math.floor(division));
            }
        }
    }
}
