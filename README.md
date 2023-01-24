
# JCompolier
JCompiler is a compiler developed by Java, which handles the following syntax structure. To increase JCompiler's cross-platform capabilities, it borrows from the JVM and ASM to compile the code as .class files.\
[Lexical Structure](https://docs.google.com/document/d/e/2PACX-1vQkcCcgs0RHqyYil4ybxeUAK14aWVBMjkJfyICYjGyCM-Hm1XI602WKuiHCtU1itNO6tXC0vgTb3jnw/pub)\
[Phrase Structure](https://docs.google.com/document/d/1nuv45jnBOFuhBAV2PWLCNBURWSlmTsPKkfy--kZedDA/edit?usp=sharing)


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
