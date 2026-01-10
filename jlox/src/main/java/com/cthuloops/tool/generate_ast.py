from dataclasses import dataclass
from enum import StrEnum
from os import path
import sys

class Expression_Type(StrEnum):
    ASSIGN = "Assign"
    BINARY = "Binary"
    GROUPING = "Grouping"
    LITERAL = "Literal"
    UNARY = "Unary"
    VARIABLE = "Variable"


class Statement_Type(StrEnum):
    EXPR = "Expr"
    PRINT = "Print"
    VAR = "Var"


class Parameter_Type(StrEnum):
    EXPRESSION = "Expression"
    TOKEN = "Token"
    OBJECT = "Object"


@dataclass(frozen=True)
class Parameter:
    param_type: Parameter_Type
    param_name: str

    def __str__(self):
        return f"{self.param_type} {self.param_name}"


@dataclass(frozen=True)
class AST_Class:
    base_name: str
    name: Expression_Type | Statement_Type
    parameters: list[Parameter]

    def __str__(self):
        return ''.join([
            self.__generate_class_decl(),
            self.__generate_constructor_decl(),
            self.__generate_fields(),
            self.__generate_constructor_end(),
            self.__generate_visitor_method(),
            self.__generate_final_fields(),
            self.__generate_class_end()
        ])

    def __generate_class_decl(self) -> str:
        return f"  static class {self.name} extends {self.base_name} {{\n"

    def __generate_constructor_decl(self) -> str:
        params = [str(param) for param in self.parameters]
        return f"    {self.name}({', '.join(params)}) {{\n"

    def __generate_fields(self) -> str:
        return "".join(f"        this.{p.param_name} = {p.param_name};\n"
                       for p in self.parameters)

    def __generate_constructor_end(self) -> str:
        return "    }\n\n"

    def __generate_visitor_method(self) -> str:
        override = "    @Override\n"
        accept = "    <R> R accept(Visitor<R> visitor) {\n"
        ret = f"        return visitor.visit{self.name}{self.base_name}(this);\n"
        end = "    }\n\n"
        return "".join([override, accept, ret, end])

    def __generate_final_fields(self) -> str:
        return "".join(f"    final {str(p)};\n" for p in self.parameters)

    def __generate_class_end(self) -> str:
        return "  }\n\n"


def build_expressions(base_name: str = "Expression") -> list[AST_Class]:
    assign = AST_Class(base_name, Expression_Type.ASSIGN,
                    [
                        Parameter(Parameter_Type.TOKEN, "name"),
                        Parameter(Parameter_Type.EXPRESSION, "value")
                    ])

    binary = AST_Class(base_name, Expression_Type.BINARY, 
                    [
                        Parameter(Parameter_Type.EXPRESSION, "left"),
                        Parameter(Parameter_Type.TOKEN, "operator"),
                        Parameter(Parameter_Type.EXPRESSION, "right")
                    ])

    grouping = AST_Class(base_name, Expression_Type.GROUPING,
                    [
                        Parameter(Parameter_Type.EXPRESSION, "expression")
                    ])

    literal = AST_Class(base_name, Expression_Type.LITERAL,
                    [
                        Parameter(Parameter_Type.OBJECT, "value")
                    ])

    unary = AST_Class(base_name, Expression_Type.UNARY,
                    [
                        Parameter(Parameter_Type.TOKEN, "operator"),
                        Parameter(Parameter_Type.EXPRESSION, "right")
                    ])
    variable = AST_Class(base_name, Expression_Type.VARIABLE,
                    [
                        Parameter(Parameter_Type.TOKEN, "name")
                    ])

    return [assign, binary, grouping, literal, unary, variable]


def build_statements(base_name: str = "Statement") -> list[AST_Class]:
    expr = AST_Class(base_name, Statement_Type.EXPR,
                    [
                        Parameter(Parameter_Type.EXPRESSION, "expression")
                    ])
    prnt = AST_Class(base_name, Statement_Type.PRINT,
                    [
                        Parameter(Parameter_Type.EXPRESSION, "expression")
                    ])
    vrbl = AST_Class(base_name, Statement_Type.VAR,
                    [
                        Parameter(Parameter_Type.TOKEN, "name"),
                        Parameter(Parameter_Type.EXPRESSION, "initializer")
                    ])

    return [expr, prnt, vrbl]



def generate_visitor_interface(base_name: str, expression_types: list[str]
                               ) -> str:
    start = "  interface Visitor<R> {\n"
    types = "".join(f"    R visit{name}{base_name}({name} {base_name.casefold()});\n"
                    for name in expression_types)
    end = "  }\n\n"
    return start + types + end


def base_accept_method() -> str:
    comment = "  // Base accept method\n"
    method = "  abstract <R> R accept(Visitor<R> visitor);\n"
    return comment + method


def define_ast(output_dir: str, base_name: str, ast: list[AST_Class]) -> None:
    filepath = path.join(output_dir, f"{base_name}.java")
    enum_type = type(ast[0].name)
    header = [
"""// This file was generated using tool/generate_ast.py any changes made
// here may be overwritten.
""",
        "package com.cthuloops.jlox;\n\n",
        "import java.util.List;\n\n",
        f"abstract class {base_name} {{\n"
    ]
    with open(filepath, "w", encoding="utf8") as f:
        f.writelines(header)
        f.write(generate_visitor_interface(base_name,
                                           [str(name) for name in enum_type]))
        for item in ast:
            f.write(str(item))

        f.write(base_accept_method())
        f.write("}\n")


def main():
    if len(sys.argv) != 2:
        print("Usage: generate_ast <output directory>", file=sys.stderr)
        exit(64)

    define_ast(sys.argv[1], "Expression", build_expressions())
    define_ast(sys.argv[1], "Statement", build_statements())


if __name__ == "__main__":
    main()