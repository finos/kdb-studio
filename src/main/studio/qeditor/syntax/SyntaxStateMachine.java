package studio.qeditor.syntax;

import java.util.*;

public class SyntaxStateMachine {

    private Transition[] transitions;
    private final List<Rule> rules = new ArrayList<>();

    private final static int MAX_CHAR=128;

    public enum Action {LooksLike, Match, MatchPrev};

    public SyntaxStateMachine() {
    }

    public void add(Enum fromState, String chars, Enum nextState, QToken token, Action action) {
        rules.add(new Rule(fromState, chars, nextState, token, action));
    }

    public String getChars(Enum state) {
        StringBuilder chars = new StringBuilder();
        for (Rule rule:rules) {
            if (rule.fromState != state) continue;
            chars.append(rule.chars);
        }
        return chars.toString();
    }

    public void init() {
        validateStateMachine();
        transitions = new Transition[getAllStates().length];
        for (int index = 0; index< transitions.length; index++) {
            transitions[index] = new Transition();
        }
        for(Rule rule: rules) {
            Transition transition = transitions[rule.fromState.ordinal()];
            if (rule.chars.length() == 0) {
                if (transition.defaultNextState != null) {
                    throw new IllegalStateException("Wrong state machine: duplicate default rule for " + rule.fromState);
                }
                transition.defaultNextState = rule.next;
            } else {
                for (char ch: rule.chars.toCharArray()) {
                    if (transition.nextState[ch] != null) {
                        throw new IllegalStateException("Wrong state machine: duplicate rule for " + rule.fromState + "; char " + ch);
                    }
                    transition.nextState[ch] = rule.next;
                }
            }
        }
    }

    public Next getNext(Enum state, char ch) {
        Transition transition = transitions[state.ordinal()];
        if (ch<MAX_CHAR) {
            Next next = transition.nextState[ch];
            if (next != null) return next;
        }
        if (transition.defaultNextState != null) return transition.defaultNextState;
        return null;
    }

    private static class Transition {
        Next defaultNextState;
        Next[] nextState;
        public Transition() {
            defaultNextState = null;
            nextState = new Next[MAX_CHAR];
            Arrays.fill(nextState, null);
        }
    }

    public static class Next {
        Enum nextState;
        QToken token;
        Action action;
        Next(Enum nextState, QToken token, Action action) {
            this.nextState = nextState;
            this.token = token;
            this.action = action;
        }
    }

    private static class Rule {
        Enum fromState;
        String chars;
        Next next;
        Rule(Enum fromState, String chars, Enum nextState, QToken token, Action action) {
            this.fromState = fromState;
            this.chars = chars;
            this.next = new Next(nextState, token, action);
        }
    }

    private Enum[] getAllStates() {
        return rules.get(0).fromState.getClass().getEnumConstants();
    }

    private void validateStateMachine() {
        Set<Enum> allStates = new HashSet<>(Arrays.asList(getAllStates()));

        Set<Enum> fromStates = new HashSet<>(allStates);
        Set<Enum> nextStates = new HashSet<>(allStates);
        for (Rule rule:rules) {
            fromStates.remove(rule.fromState);
            nextStates.remove(rule.next.nextState);
        }
        if (fromStates.size()>0) {
            throw new IllegalStateException("Wrong state machine: from states missing: " + fromStates);
        }
        if (nextStates.size()>0) {
            throw new IllegalStateException("Wrong state machine: from states missing: " + nextStates);
        }
    }

}
