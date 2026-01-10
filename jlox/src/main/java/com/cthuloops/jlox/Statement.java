// This file was generated using tool/generate_ast.py any changes made
// here may be overwritten.
package com.cthuloops.jlox;

import java.util.List;

abstract class Statement {
  interface Visitor<R> {
    R visitExprStatement(Expr statement);
    R visitPrintStatement(Print statement);
  }

  static class Expr extends Statement {
    Expr(Expression expression) {
        this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visitExprStatement(this);
    }

    final Expression expression;
  }

  static class Print extends Statement {
    Print(Expression expression) {
        this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visitPrintStatement(this);
    }

    final Expression expression;
  }

  // Base accept method
  abstract <R> R accept(Visitor<R> visitor);
}
