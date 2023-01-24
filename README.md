## Getting Started

Welcome to the VS Code Java world. Here is a guideline to help you get started to write Java code in Visual Studio Code.

## Folder Structure

The workspace contains two folders by default, where:

- `src`: the folder to maintain sources
- `lib`: the folder to maintain dependencies

Meanwhile, the compiled output files will be generated in the `bin` folder by default.

> If you want to customize the folder structure, open `.vscode/settings.json` and update the related settings there.

## Dependency Management

The `JAVA PROJECTS` view allows you to manage your dependencies. More details can be found [here](https://github.com/microsoft/vscode-java-dependency#manage-dependencies).

## Code Outline

This Compiler mainly consists of 5 units.
### 1. Lexical Analysis
Lexer will break a code sentence into tokens, remove useless words, and determine the type of token, and finally record the location of each token.
```
Interface: ILexer, IToken
Class: Lexer, Token
FactoryClass: CompilerComponentFactory
```
### 2. Parsing and AST Generation
[Phrase Structure](https://docs.google.com/document/d/1nuv45jnBOFuhBAV2PWLCNBURWSlmTsPKkfy--kZedDA/edit?usp=sharing)
Parser will return a Abstract Syntax Tree(AST) for future using. Using Visitor Pattern to visit AST.
```
The grammar is LL(1), so I used a top down parsing.
Interface: IParser
Class: Parser
```
### 3. Scopes
Identifiers and Procedures have their own scopes. They can only be used in their own scope. By traversing ASTs, I can define their scopes.
```
Add new properties <nest> in Identifiers and Procedures.
Class: JDScopeVisitor
```
### 4. Type Infering
Some of the identifiers haven't been defined since they appeared. To infer their types, I built an ASTVisitor to multiple traverse ASTs. After that, their type can be infered.
```
Class: JDTypeCheckVisitor
```
### 5. Code Generater
Using ASM to generate java class files.
```
Class: CodeGenVisitor
```
