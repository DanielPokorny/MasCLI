/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mas.machine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provádí interpretaci vlastního programu v BASICu.
 *
 * Obecné
 * ======
 * Každý řádek začíná názvem proměnné, příkazem nebo obsahuje návěští.
 *
 * Priorita operátorů
 * ==================
 * |priorita|operátor                 |poznámka                        |
 * |--------|:-----------------------:|--------------------------------|
 * |2       |funkce                   |                                |
 * |3       |^ (mocnina)              |                                |
 * |4       |polarita (+/-)           |                                |
 * |5       |* a / (násobení a dělení)|                                |
 * |6       |+ a -                    |+ funguje i na spojování řetězců|
 * |7       |= (rovná se)             |                                |
 * |8       |AND a OR                 |Jako logická 1 (pravda) se bere i číslo větší než 0.|
 *
 * Funkce
 * ======
 * |název                       |popis                                                                                 |
 * |----------------------------|--------------------------------------------------------------------------------------|
 * |ABS(x)                      |Vrací absolutní hodnotu parametru.                                                    |
 * |                            |Formát: ABS(x)                                                                        |
 * |                            |Parametr: x je číslo.                                                                 |
 * |ATN(x)                      |Vrací arcustangens parametru.                                                         |
 * |                            |Formát: ATN(x)                                                                        |
 * |                            |Parametr: x je úhel v radiánech.                                                      |
 * |BIT(x, y)                   |Vrací hodnotu zadaného bitu ze zadaného bajtu.                                        |
 * |                            |Formát: BIT(x, y)                                                                     |
 * |                            |Parametry:                                                                            |
 * |                            |x je numerická proměnná                                                               |
 * |                            |y je číslo daného bitu                                                                |
 * |EXTRACT(re$, origin$)       |Vrátí část řetězce origin$, odpovídající regulárnímu výrazu re$.                      |
 * |                            |Parametry:                                                                            |
 * |                            |re$ je regulární výraz, který bude vyhledáván                                         |
 * |                            |origin$ je řetězec, který bude prohledáván                                            |
 * |NOT(X)                      |Vrací negovanou hodnotu logického výrazu X.                                           |
 * |                            |Formát: NOT(X)                                                                        |
 * |                            |Parametr: X je logický výraz.                                                         |
 * |                            |Příklad: NOT(A > 3)                                                                   |
 * |REPLACE(re$, repl$, origin$)|Vrátí řetězec s nahrazenými znaky podle regulárního výrazu, původní řetězec bude nezměněn.|
 * |                            |Parametry:                                                                            |
 * |                            |re$ je regulární výraz, který bude nahrazen                                           |
 * |                            |repl$ je řetězec, kterým bude nahrazen regulární výraz                                |
 * |                            |origin$ je řetězec, ve kterém bude provedeno nahrazení                                |
 * |UTF(a$)                     |Vrací hodnotu prvního znaku zadaného řetězce, která je jeho reprezentací v kódu UTF-16, překonvertovaná na dekadické číslo.|
 * |                            |Formát: ASC(a$)                                                                       |
 * |                            |Parametr: a$ je řetězcová proměnná.                                                   |
 *
 * Proměnné
 * ========
 * - Nesmí být shodné s příkazem.
 * - Musí začínat písmenem.
 * - Nesmí obsahovat mezeru nebo znak operátoru.
 *
 * Příkazy
 * =======
 * CLOSE line$
 * -----------
 * Zruší komunikační linku line$. I když není prázdná, je zrušena okamžitě.
 *
 * END
 * ---
 * Ukončí provádění programu.
 *
 * FOR ... TO ... STEP ... NEXT
 * ----------------------------
 * Vykonává v cyklu příkazy mezi FOR a NEXT.
 * Formát: FOR v=e1 TO e2 STEP e3
 *
 * Parametry:
 * - v - řídící proměnná cyklu,
 * - e1 - počáteční hodnota,
 * - e2 - koncová hodnota. Pokud proměnná nabyde této hodnoty nebo je větší, cyklus se ukončí.
 * - e3 - krok cyklu; pokud je 1, netřeba uvádět. Po vykonání skupiny příkazů medzi FOR a NEXT se hodnota řídící proměnné zvýší o hodnotu e3 a cyklus pokračuje.
 *
 * Příklad:
 * ~~~~~
 * REM SOUCET CISEL ARITMETICKE POSLOUPNOSTI
 * A1 = 1
 * AN = 10
 * D = 2
 * N=0
 * S=0
 * SN=0
 * FOR X = A1 TO AN STEP D
 *  S = S + X
 * NEXT X
 * ~~~~~
 *
 * GOSUB ... RETURN
 * ----------------
 * Přejde v programu na řádek označený návěštím v parametru. Po zpracování příkazu RETURN se vrátí vykonávání programu do místa za příkazem GOSUB.
 *
 * Příklad:
 * ~~~~~
 * A = 0
 * Dokola:
 * A = A + 1
 * PRINT A
 * GOSUB Podtrhni
 * GOTO Dokola
 * Podtrhni:
 *  PRINT "-------"
 * RETURN
 * ~~~~~
 *
 * GOTO label
 * ----------
 * Přejde v programu na řádek označený návěštím v parametru.
 *
 * IF ... THEN
 * -----------
 * Podmíněné větvení programu. Pokud je podmínka splněná, vykoná se příkaz uvedený za THEN.
 * Formát: IF c THEN n
 * Parametry:
 * - c - testovaný výraz, ktorého hodnota je buď TRUE nebo FALSE.
 * - n - je příkaz, který se vykoná, pokud je podmínka splněná.
 *
 * INPUT addr$, var$
 * Načte string z linky addr$ do proměnné var$.
 *
 * OPEN LINE a$
 * ------------
 * Vytvoří komunikační linku s názvem a$. Název linky je case senzitiv.
 *
 * PRINT line$, a$
 * ---------------
 * Zapíše na hodnotu parametru a$ do zadané linky line$.
 *
 * Návěští
 * =======
 * Označuje místo v programu. Je možno na něj provést podmíněný i nepodmíněný skok. Musí začínat písmenem (ASCII bez diakritiky) a může obsahovat číslice.
 * Je ukončeno dvojtečnou. Jeden řádek může obsahovat pouze jedno návěští.
 *
 * Příklad:
 * ~~~~~
 * A = 0
 * Dokola:
 * PRINT A
 * GOTO Dokola
 * ~~~~~
 *
 * @author daniel
 */
public class BasicWorker extends Worker{
    /**
     * Binární operátor, který je použit při vyhodnocování výrazu.
     */
    private enum Operator_ {PLUS, MULTIPLY, DIVIDE, MINUS, POWER, EQUALS, LESS_THEN,
    GREATER_THEN, LESS_THEN_OR_EQUAL, GREATER_THEN_OR_EQUAL, INEQUAL, AND, OR};
    private Map<String, Token> variables_;
    private Map<String, Integer> labels_;
    private Deque<Object> gosubStack_;
    private Deque<String> forVariableStack_;
    private Deque<Double> forStepStack_;
    private Deque<Double> forLimitStack_;
    private Deque<Integer> forProgramCounterStack_;
    private List<String> program_;
    private int programCounter_;
    private Workspace workspace_;

    /**
     * Vytvoří nový interpreter se jménem name v zadaném workspace.
     * @param name  jméno interpreteru
     * @param workspace pracovní prostor interpreteru
     */
    public BasicWorker(String name, Workspace workspace) {
        this.programCounter_ = 0;
        this.program_ = new ArrayList<>();
        this.forProgramCounterStack_ = new LinkedList<>();
        this.forLimitStack_ = new LinkedList<>();
        this.forStepStack_ = new LinkedList<>();
        this.forVariableStack_ = new LinkedList<>();
        this.gosubStack_ = new LinkedList<>();
        this.labels_ = new TreeMap<>();
        this.variables_ = new TreeMap<>();
        setWorkerName(name);
        this.workspace_ = workspace;
        this.setDaemon(true);
    }



    /**
     * Přidá řádek na konec programu v BASICu.
     * @param line řádek v BASICu
     */
    public void addLine(String line) {
        program_.add(line);
    }

    /**
     * Spustí normální běh programu v BASICu.
     */
    @Override
    public void run() {
        fillLabels_();
        while(programCounter_ < program_.size()) {
            try {
                interpret_(program_.get(programCounter_));
            } catch (IOException | InterruptedException ex) {
                Logger.getLogger(BasicWorker.class.getName()).log(Level.SEVERE, null, ex);
            }
            programCounter_++;
        }
    }

    /**
     * Zpracuje jeden řádek v BASICu.
     * @param line  řádek v BASICu
     * @throws IOException
     * @throws InterruptedException
     */
    private void interpret_(String line) throws IOException, InterruptedException {
        line = line.trim();
        if(line.length() > 0) {
            if(line.matches("[a-zA-Z]+[a-zA-Z0-9]+:")) {
//                System.out.println("label");
            } else {
                String firstWord = "";
                int spaceIndex = line.indexOf(" ");
                int equalIndex = line.indexOf("=");
                if(spaceIndex > -1 && equalIndex > -1) {
                    firstWord = line.substring(0, Math.min(equalIndex, spaceIndex)).toUpperCase();
                } else if(spaceIndex > -1 && equalIndex == -1){
                    firstWord = line.substring(0, spaceIndex).toUpperCase();
                } else if(spaceIndex == -1 && equalIndex > -1){
                    firstWord = line.substring(0, equalIndex).toUpperCase();
                } else if(spaceIndex == -1 && equalIndex == -1){
                    firstWord = line.toUpperCase();
                }

                switch(firstWord) {
                    case "CLOSE":
                        {
                            String parameter = line.substring(5);
                            workspace_.closeCommLine(evaluate_(parameter).getString());
                        }
                        break;
                    case "END":
                        programCounter_ = program_.size();
                        break;
                    case "FOR":
                        {
                            String forLine = line.substring(3).trim();
                            String variable = forLine.substring(0, forLine.indexOf("=")).trim().toUpperCase();
                            forLine = forLine.substring(forLine.indexOf("=") + 1);
                            String fromValue = forLine.substring(0, forLine.indexOf(" TO "));
                            forLine = forLine.substring(forLine.indexOf(" TO ") + 3);
                            Double step = new Double(1);
                            Double toValue = null;
                            if(forLine.indexOf(" STEP ") > -1) {
                                toValue = evaluate_(forLine.substring(0, forLine.indexOf(" STEP "))).getDouble();
                                forLine = forLine.substring(forLine.indexOf(" STEP ") + 5);
                                step = evaluate_(forLine).getDouble();
                            } else {
                                toValue = evaluate_(forLine).getDouble();
                            }

                            Token fromToken = new Token(Token.Type.NUMBER, evaluate_(fromValue).getDouble());

                            if(toValue.compareTo(new Double(0)) < 0) {
                                fromToken = new Token(Token.Type.NUMBER, toValue);
                                toValue = evaluate_(fromValue).getDouble();
                            }

                            if(variables_.containsKey(variable)) {
                                variables_.replace(variable, fromToken);
                            } else {
                                variables_.put(variable, fromToken);
                            }

                            forVariableStack_.push(variable);
                            forStepStack_.push(step);
                            forProgramCounterStack_.push(programCounter_);
                            forLimitStack_.push(toValue);
                        }
                        break;
                    case "GOSUB":
                        {
                            String label = line.substring(5).trim();
                            gosubStack_.push(programCounter_);
                            programCounter_ = labels_.get(label);
                        }
                        break;
                    case "GOTO":
                        {
                            String label = line.substring(4).trim();
                            programCounter_ = labels_.get(label);
                        }
                        break;
                    case "IF":
                        {
                            String expression = line.substring(2, line.indexOf(" THEN "));
                            String command = line.substring(line.indexOf(" THEN ") + 5);
                            Token value = evaluate_(expression);
                            if(value.getBoolean()) {
                                interpret_(command);
                            }
                        }
                        break;
                    case "INPUT":
                        {
                            String parameter = line.substring(5).trim();
                            String address = parameter.substring(0, parameter.indexOf(",")).trim();
                            String variable = parameter.substring(parameter.indexOf(",") + 1).trim();
                            String value = null;
                            while (value == null) {
                                try {
                                    value = workspace_.getMessage(evaluate_(address).getString()).toString();
                                } catch (Exception ex) {
                                    System.out.println("Warning: INPUT from commline \"" + address + "\" failed.");
                                    Thread.sleep(10000);
                                }
                            }

                            interpret_(variable + " = \"" + value + "\"");
                        }
                        break;
                    case "NEXT":
                        {
                            String variable = forVariableStack_.peek();
                            Double step = forStepStack_.peek();
                            Double limit = forLimitStack_.peek();

                            Double value = variables_.get(variable).getDouble() + step;

                            if((step.compareTo(new Double(0)) > 0 && value.compareTo(limit) > 0)
                                    || (step.compareTo(new Double(0)) < 0 && value.compareTo(limit) < 0)) {
                                forLimitStack_.pop();
                                forProgramCounterStack_.pop();
                                forStepStack_.pop();
                                forVariableStack_.pop();
                            } else {
                                programCounter_ = forProgramCounterStack_.peek();
                                variables_.get(variable).setValue(value);
                            }
                        }
                        break;
                    case "OPEN":
                        {
                            String parameter = line.substring(4);
                            if(parameter.startsWith(" LINE ")) {
                                parameter = parameter.substring(6);
                            }
                            workspace_.addCommLine(evaluate_(parameter).getString());
                        }
                        break;
                    case "PRINT":
                        String parameter = line.substring(5);
                        int pDivider = 0;

                        int quotationCount = 0;
                        for(int printDivider = 0; printDivider < parameter.length(); printDivider++) {
                            if(parameter.charAt(printDivider) == '\"') {
                                quotationCount++;
                            }

                            if(pDivider == 0 && parameter.charAt(printDivider) == ',' && quotationCount % 2 == 0 ) {
                                pDivider = printDivider;
                            }
                        }

                        String a = parameter.substring(0, pDivider);
                        String address = evaluate_(parameter.substring(0, pDivider)).getString();
                        String message = evaluate_(parameter.substring(pDivider + 1)).getString();

                        workspace_.sendMessage(address, message);

                        break;
                    case "RETURN":
                        programCounter_ = (int) gosubStack_.pop();
                        break;

// proměnná
                    default:
                        String variableName = firstWord;
                        String valueString = line.substring(equalIndex + 1);
                        Token value = evaluate_(valueString);
                        variables_.put(variableName, value);
                        break;
                }
            }
        }
    }

    /**
     * Vyhodnotí zadaný výraz.
     * Priority operátorů:
     * 1. výrazy v závorkách     ( )
     * 2. funkce
     * 3. mocnina ^
     * 4. polarita + -
     * 5. násobení *
     *    dělení /
     * 6. sčítání +
     *    odčítání -
     * 7. relace "je rovný" =
     *    relace "je menší" <
     *    relace "je větší" >
     *    relace "je rovný nebo menší" <=
     *    relace "nerovná se" <>
     *    relace "je rovný nebo větší" >=
     * 8. logický součin AND
     *    logický součet OR
     *
     * @param input aritmetický nebo řetězcový výraz
     * @return výsledek vyhodnocení výrazu
     */
    private Token evaluate_(String input) {
        Token returnValue = null;
        input = input.trim();
        if(input.length() > 0) {
            while(input.startsWith("(") && input.endsWith(")")) {
                input = input.substring(1);
                input = input.substring(0, input.length() - 1);
            }

            int bracketCount = 0;
            int quoteCount = 0;
            int leftEnd = -1;
            int rightStart = -1;
            int dividerPriority = 0;
            Operator_ operator = null;

            for(int i = 0; i < input.length(); i++) {
                String chr = input.substring(i, i + 1);
                if(chr.equals("(")) {
                    bracketCount++;
                }
                if(chr.equals(")")) {
                    bracketCount--;
                }
                if(chr.equals("\"")) {
                    quoteCount++;
                }

                if(bracketCount == 0 && quoteCount % 2 == 0) {
                    try {
                        if(input.substring(i, i + 5).matches(" AND ")) {
                            if(dividerPriority <= 8) {
                                leftEnd = i;
                                rightStart = i + 5;
                                dividerPriority = 8;
                                operator = Operator_.AND;
                            }
                        }
                        if(input.substring(i, i + 4).matches(" OR ")) {
                            if(dividerPriority <= 8) {
                                leftEnd = i;
                                rightStart = i + 4;
                                dividerPriority = 8;
                                operator = Operator_.OR;
                            }
                        }
                    } catch(Exception e) {

                    }
                    if(chr.equals("=") && input.substring(i-1, i).matches("[^<>]+")) {
                        if(dividerPriority <= 7) {
                            leftEnd = i;
                            rightStart = i + 1;
                            dividerPriority = 7;
                            operator = Operator_.EQUALS;
                        }
                    } else if(chr.equals("<") && input.substring(i+1, i+2).matches("[^=>]")) {
                        if(dividerPriority <= 7) {
                            leftEnd = i;
                            rightStart = i + 1;
                            dividerPriority = 7;
                            operator = Operator_.LESS_THEN;
                        }
                    } else if(chr.equals(">") && (input.substring(i-1, i).matches("[^<]") && input.substring(i+1, i+2).matches("[^=]"))) {
                        if(dividerPriority <= 7) {
                            leftEnd = i;
                            rightStart = i + 1;
                            dividerPriority = 7;
                            operator = Operator_.GREATER_THEN;
                        }
                    } else if(chr.equals("<") && (input.substring(i+1, i+2).equals("="))) {
                        if(dividerPriority <= 7) {
                            leftEnd = i;
                            rightStart = i + 2;
                            dividerPriority = 7;
                            operator = Operator_.LESS_THEN_OR_EQUAL;
                        }
                    } else if(chr.equals(">") && (input.substring(i+1, i+2).equals("="))) {
                        if(dividerPriority <= 7) {
                            leftEnd = i;
                            rightStart = i + 2;
                            dividerPriority = 7;
                            operator = Operator_.GREATER_THEN_OR_EQUAL;
                        }
                    } else if(chr.equals("<") && (input.substring(i+1, i+2).equals(">"))) {
                        if(dividerPriority <= 7) {
                            leftEnd = i;
                            rightStart = i + 2;
                            dividerPriority = 7;
                            operator = Operator_.INEQUAL;
                        }
                    } else if(chr.equals("+")) {
                        if(dividerPriority <= 6) {
                            leftEnd = i;
                            rightStart = i + 1;
                            dividerPriority = 6;
                            operator = Operator_.PLUS;
                        }
                    } else if(chr.equals("-")) {
                        int i2 = i;
                        boolean isOperator = true;
                        while(i2 > -1 && isOperator){
                            if(input.substring(i2, i2+1).matches("[-*/+]")) {
                                isOperator = false;
                            }
                            if(input.substring(i2, i2+1).equals(")")) {
                                i2 = -1;
                            } else {
                                i2++;
                            }
                        }
                        if(isOperator) {
                            if(dividerPriority <= 6) {
                                leftEnd = i;
                                rightStart = i + 1;
                                dividerPriority = 6;
                                operator = Operator_.MINUS;
                            }
                        }
                    } else if(chr.equals("*")) {
                        if(dividerPriority <= 5) {
                            leftEnd = i;
                            rightStart = i + 1;
                            dividerPriority = 5;
                            operator = Operator_.MULTIPLY;
                        }
                    } else if(chr.equals("/")) {
                        if(dividerPriority <= 5) {
                            leftEnd = i;
                            rightStart = i + 1;
                            dividerPriority = 5;
                            operator = Operator_.DIVIDE;
                        }
                    } else if(chr.equals("^")) {
                        if(dividerPriority <= 3) {
                            leftEnd = i;
                            rightStart = i + 1;
                            dividerPriority = 3;
                            operator = Operator_.POWER;
                        }
                    }
                }
            }

            if(dividerPriority > 0) {
                String left = input.substring(0, leftEnd);
                String right = input.substring(rightStart);
                Token leftEval = evaluate_(left);
                Token rightEval = evaluate_(right);
                Double le;
                Double re;
                switch (operator) {
                    case PLUS:
                        if(leftEval.isString() && rightEval.isString()) {
                            returnValue = new Token(Token.Type.STRING, leftEval.getString() + rightEval.getString());
                        } else {
                            returnValue = new Token(Token.Type.NUMBER, leftEval.getDouble() + rightEval.getDouble());
                        }
                        break;
                    case MINUS:
                        returnValue = new Token(Token.Type.NUMBER, leftEval.getDouble() - rightEval.getDouble());
                        break;
                    case MULTIPLY:
                        returnValue = new Token(Token.Type.NUMBER, leftEval.getDouble() * rightEval.getDouble());
                        break;
                    case DIVIDE:
                        returnValue = new Token(Token.Type.NUMBER, leftEval.getDouble() / rightEval.getDouble());
                        break;
                    case POWER:
                        returnValue = new Token(Token.Type.NUMBER,
                                Math.pow(leftEval.getDouble(), rightEval.getDouble()));
                        break;
                    case EQUALS:
                        if(leftEval.isNumber()) {
                            le = leftEval.getDouble();
                            re = rightEval.getDouble();
                            if(le.equals(re)) {
                                returnValue = new Token(Token.Type.BOOLEAN , Boolean.TRUE);
                            } else {
                                returnValue = new Token(Token.Type.BOOLEAN , Boolean.FALSE);
                            }
                        } else {
                            if(leftEval.getString().equals(rightEval.getString())) {
                                returnValue = new Token(Token.Type.BOOLEAN , Boolean.TRUE);
                            } else {
                                returnValue = new Token(Token.Type.BOOLEAN , Boolean.FALSE);
                            }
                        }
                        break;
                    case LESS_THEN:
                        le = leftEval.getDouble();
                        re = rightEval.getDouble();
                        if(le.compareTo(re) < 0) {
                            returnValue = new Token(Token.Type.BOOLEAN, Boolean.TRUE);
                        } else {
                            returnValue = new Token(Token.Type.BOOLEAN, Boolean.FALSE);
                        }
                        break;
                    case GREATER_THEN:
                        le = leftEval.getDouble();
                        re = rightEval.getDouble();
                        if(le.compareTo(re) > 0) {
                            returnValue = new Token(Token.Type.BOOLEAN, Boolean.TRUE);
                        } else {
                            returnValue = new Token(Token.Type.BOOLEAN, Boolean.FALSE);
                        }
                        break;
                    case LESS_THEN_OR_EQUAL:
                        le = leftEval.getDouble();
                        re = rightEval.getDouble();
                        if(le.compareTo(re) <= 0) {
                            returnValue = new Token(Token.Type.BOOLEAN, Boolean.TRUE);
                        } else {
                            returnValue = new Token(Token.Type.BOOLEAN, Boolean.FALSE);
                        }
                        break;
                    case GREATER_THEN_OR_EQUAL:
                        le = leftEval.getDouble();
                        re = rightEval.getDouble();
                        if(le.compareTo(re) >= 0) {
                            returnValue = new Token(Token.Type.BOOLEAN, Boolean.TRUE);
                        } else {
                            returnValue = new Token(Token.Type.BOOLEAN, Boolean.FALSE);
                        }
                        break;
                    case INEQUAL:
                        le = leftEval.getDouble();
                        re = rightEval.getDouble();
                        if(le.compareTo(re) != 0) {
                            returnValue = new Token(Token.Type.BOOLEAN, Boolean.TRUE);
                        } else {
                            returnValue = new Token(Token.Type.BOOLEAN, Boolean.FALSE);
                        }
                        break;
                    case AND:
                        Boolean lb = true;
                        Boolean rb = true;
                        if(leftEval.isBoolean()) {
                            lb = leftEval.getBoolean();
                        } else if(leftEval.isNumber()) {
                            if(leftEval.getDouble().compareTo(Double.valueOf(0)) <= 0) {
                                lb = false;
                            }
                        }
                        if(rightEval.isBoolean()) {
                            rb = rightEval.getBoolean();
                        } else if(rightEval.isNumber()) {
                            if(rightEval.getDouble().compareTo(Double.valueOf(0)) <= 0) {
                                rb = false;
                            }
                        }
                        returnValue = new Token(Token.Type.BOOLEAN, lb && rb);
                        break;
                    case OR:
                        Boolean lob = true;
                        Boolean rob = true;
                        if(leftEval.isBoolean()) {
                            lob = leftEval.getBoolean();
                        } else if(leftEval.isNumber()) {
                            if(leftEval.getDouble().compareTo(Double.valueOf(0)) <= 0) {
                                lob = false;
                            }
                        }
                        if(rightEval.isBoolean()) {
                            rob = rightEval.getBoolean();
                        } else if(rightEval.isNumber()) {
                            if(rightEval.getDouble().compareTo(Double.valueOf(0)) <= 0) {
                                rob = false;
                            }
                        }
                        returnValue = new Token(Token.Type.BOOLEAN, lob || rob);
                        break;
                }

            } else {
                String startString = input.toUpperCase().substring(0, input.indexOf("(") + 1);
                ArrayList<String> parameters = null;
                String paramString = input.substring(startString.length(), input.length() - 1);
                parameters = parseParameters(paramString);

                switch(startString) {
                    case "ABS(":
                        {
                            Double value = evaluate_(parameters.get(0)).getDouble();
                            returnValue = new Token(Token.Type.NUMBER, Math.abs(value));
                        }
                        break;
                    case "ATN(":
                        {
                            Double value = evaluate_(parameters.get(0)).getDouble();
                            returnValue = new Token(Token.Type.NUMBER, Math.atan(value));
                        }
                        break;
                    case "BIT(":
                        {
                            int parameter1 = evaluate_(parameters.get(0)).getDouble().intValue();
                            int parameter2 = evaluate_(parameters.get(1)).getDouble().intValue();
                            returnValue = new Token(Token.Type.NUMBER, (double) ((parameter1 >> parameter2) & 1));
                        }
                        break;
                    case "EXTRACT(":
                        {
                            Pattern pattern = Pattern.compile(evaluate_(parameters.get(0)).getString());
                            String string = evaluate_(parameters.get(1)).getString();
                            Matcher matcher = pattern.matcher(string);
                            if (matcher.find()) {
                                returnValue = new Token(Token.Type.STRING, matcher.group(0));
                                System.out.println("");
                                        
                            } else {
                                returnValue = new Token(Token.Type.STRING, "");
                            }
                        }
                        break;
                    case "NOT(":
                        {
                            Token parameter = evaluate_(parameters.get(0));
                            if(parameter.getBoolean() == true) {
                                returnValue = new Token(Token.Type.BOOLEAN, false);
                            } else {
                                returnValue = new Token(Token.Type.BOOLEAN, false);
                            }
                        }
                        break;
                    case "REPLACE(":
                        {
                            String regex = evaluate_(parameters.get(0)).getString();
                            String replacement = evaluate_(parameters.get(1)).getString();
                            String string = evaluate_(parameters.get(2)).getString();
                            returnValue = new Token(Token.Type.STRING, string.replaceAll(regex, replacement));
                        }
                        break;
                    case "UTF(":
                        {
                            Token parameter = evaluate_(parameters.get(0));
                            int code = (int) parameter.getString().charAt(0);
                            returnValue = new Token(Token.Type.NUMBER, (double) code);
                        }
                        break;
                    default:
                        if(variables_.containsKey(input)) {
                            returnValue = variables_.get(input);
                        } else {
                            if(input.startsWith("\"") && input.endsWith("\"")) {
                                returnValue = new Token(Token.Type.STRING, input.replace("\"", ""));
                            } else {
                                returnValue = new Token(Token.Type.NUMBER, Double.parseDouble(input));
                            }
                        }
                        break;
                }
            }
        }
        return returnValue;
    }

    /**
     * Prohledá program v BASICu  a naplní seznam labelů.
     */
    private void fillLabels_() {
        for(int i = 0; i < program_.size(); i++) {
            if(program_.get(i).matches("[a-zA-Z]+[a-zA-Z0-9]+:")) {
                labels_.put(program_.get(i).substring(0, program_.get(i).length()  - 1), i);
            }
        }
    }
    
    /**
     * Vrátí seznam parametrů oddělených na vstupu čárkami. První parametr má index 0. 
     * 
     * @param parameters parametry funkce oddělené čárkami
     * @return seznam parametrů
     */
    ArrayList<String> parseParameters(String parameters) {
        ArrayList<String> returnValue = new ArrayList<>();
        if(parameters.length() > 0) {
            int divider = 0;
            int bracketCount = 0;
            int quotesCount = 0;
            String parameter = "";
            boolean notFinished = true;

            while(notFinished) {
                char character = parameters.charAt(divider);
                switch(character) {
                    case '(':
                        bracketCount++;
                        parameter = parameter + character;
                        break;
                    case ')':
                        bracketCount--;
                        parameter = parameter + character;
                        break;
                    case '\"':
                        quotesCount++;
                        parameter = parameter + character;
                        break;
                    case ',':
                        if(bracketCount == 0 && quotesCount % 2 == 0) {
                            returnValue.add(parameter);
                            parameter = "";
                            System.out.println("");
                        } else {
                            parameter = parameter + character;                        
                        }

                        break;
                    default:
                        parameter = parameter + character;
                        break;                    
                }
                divider++;
                if(divider == parameters.length()) {
                    notFinished = false;
                    if(parameter.length() > 0) {
                        returnValue.add(parameter);
                    }
                }
            }
        }
        return returnValue;
    }
}
