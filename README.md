# Jinst

## O que faz?

Pega numa aplicação Android (Java-based) e transforma o seu source code. Copia todo o projeto para uma pasta "_TRANSFORMED_" e altera os ficheiros java de acordo com os seguintes argumentos:
- arg[0] -  build system usado na app (gradle, mvn,ant); 
- arg[1] - Nome de diretoria para onde copiar projeto
- arg[2] - workspace (já nao é necessario, foi so mantido por questoes de compatibilidade);
- arg[3] - dir para projeto
- arg[4] - caminho para main manifest.xml do projeto;
- arg[5] - caminho para manifest.xml dos testes (se existir. tipicamente não existe);
- arg[6] - Orientacao do teste. Diz de que forma devem ser instrumentados os metodos da aplicacao:
			-	TestOriented - instrumenta no inicio do metodo
			-   Method Oriented - instrumenta no inicio e final do metodo( antes de um return,por exemplo)
			-   Acitivity - instrumenta no onCreate e no onDestroy de uma activity Android
- arg[7] - framework de teste para ser usada. Se for junit, é efetuada instrumentacao também dos testes unitarios da aplicação;
- arg[8] - id da aplicação (pode ser qq coisa);
- arg[9] - se é para fazer blackbox ou whitebox testing: se for blackbox o codigo original das classes não é alterado/instrumentado.


# Invocação
```
java -jar jInst.jar "-gradle" "_TRANSFORMED_" "X" "./demoProjects/N2AppTest" "./demoProjects/N2AppTest/app/src/main/AndroidManifest.xml" "-" "-TestOriented" "-junit" "N2AppTest--uminho.di.greenlab.n2apptest" "blackbox"
```
