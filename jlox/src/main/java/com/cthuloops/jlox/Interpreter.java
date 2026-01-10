package com.cthuloops.jlox;

import java.util.List;

class Interpreter implements Expression.Visitor<Object>,
                             Statement.Visitor<Void> {

    void interpret(List<Statement> statements) {
        try {
            for (Statement statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    @Override
    public Object visitLiteralExpression(Expression.Literal expression) {
        return expression.value;
    }

    @Override
    public Object visitGroupingExpression(Expression.Grouping expression) {
        return evaluate(expression.expression);
    }

    @Override
    public Object visitUnaryExpression(Expression.Unary expression) {
        Object right = evaluate(expression.right);

        switch (expression.operator.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(expression.operator, right);
                return -(double)right;
        }

        // Unreachable.
        return null;
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) {
            return;
        }
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator,
                                     Object left, Object right) {
        if (left instanceof Double && right instanceof Double) {
            return;
        }

        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    @Override
    public Object visitBinaryExpression(Expression.Binary expression) {
        Object left = evaluate(expression.left);
        Object right = evaluate(expression.right);

        switch (expression.operator.type) {
            case GREATER:
                checkNumberOperands(expression.operator, left, right);
                return (double)left > (double)right;

            case GREATER_EQUAL:
                checkNumberOperands(expression.operator, left, right);
                return (double)left >= (double)right;

            case LESS:
                checkNumberOperands(expression.operator, left, right);
                return (double)left < (double)right;

            case LESS_EQUAL:
                checkNumberOperands(expression.operator, left, right);
                return (double)left <= (double)right;

            case BANG_EQUAL:
                return !isEqual(left, right);

            case EQUAL_EQUAL:
                return isEqual(left, right);

            case MINUS:
                checkNumberOperands(expression.operator, left, right);
                return (double)left - (double)right;

            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                }

                if (left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }
                throw new RuntimeError(expression.operator,
                     "Operands must be two numbers or two strings.");

            case SLASH:
                checkNumberOperands(expression.operator, left, right);
                return (double)left / (double)right;

            case STAR:
                checkNumberOperands(expression.operator, left, right);
                return (double)left * (double)right;
        }

        // Unreachable.
        return null;
    }

    private Object evaluate(Expression expression) {
        return expression.accept(this);
    }

    private void execute(Statement statement) {
        statement.accept(this);
    }

    @Override
    public Void visitExprStatement(Statement.Expr statement) {
        evaluate(statement.expression);
        return null;
    }

    @Override 
    public Void visitPrintStatement(Statement.Print statement) {
        Object value = evaluate(statement.expression);
        System.out.println(stringify(value));
        return null;
    }
    
    private boolean isTruthy(Object object) {
        if (object == null) {
            return false;
        }
        if (object instanceof Boolean) {
            return (boolean)object;
        }
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null) {
            return false;
        }

        return a.equals(b);
    }

    private String stringify(Object object) {
        if (object == null) {
            return "nil";
        }

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }
}
