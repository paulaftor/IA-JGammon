# IA-JGammon

Para executar o código, basta entrar na pasta JGammon e executar o comando
```sh
ant run
```

Caso ainda não tenha o ant instalado, siga o passo a passo para seu sistema operacional:

### Instalando Apache Ant no Linux

1. **Verifique se o Java está instalado:**
   O Apache Ant requer o Java JDK. Para verificar se o Java está instalado, use:
   ```sh
   java -version
   ```
   Se não estiver instalado, você pode instalar o OpenJDK usando:
   ```sh
   sudo apt update
   sudo apt install default-jdk
   ```

2. **Baixe o Apache Ant:**
   Visite a página de download do Apache Ant e baixe a versão mais recente. Alternativamente, você pode usar `wget` para baixar diretamente:
   ```sh
   wget https://downloads.apache.org/ant/binaries/apache-ant-<versao>-bin.zip
   ```

3. **Extraia o arquivo baixado:**
   ```sh
   unzip apache-ant-<versao>-bin.zip
   ```

4. **Mova o Ant para um diretório apropriado (opcional):**
   ```sh
   sudo mv apache-ant-<versao> /opt/ant
   ```

5. **Configurar variáveis de ambiente:**
   Abra o arquivo `~/.bashrc` (ou `~/.bash_profile` ou `~/.profile` dependendo da sua configuração) e adicione as seguintes linhas:
   ```sh
   export ANT_HOME=/opt/ant
   export PATH=$ANT_HOME/bin:$PATH
   ```
   Para aplicar as mudanças, rode:
   ```sh
   source ~/.bashrc
   ```

6. **Verifique a instalação:**
   ```sh
   ant -version
   ```

### Instalando Apache Ant no Windows

1. **Verifique se o Java está instalado:**
   O Apache Ant requer o Java JDK. Para verificar se o Java está instalado, abra o Prompt de Comando e digite:
   ```sh
   java -version
   ```
   Se não estiver instalado, faça o download e instale o JDK do site oficial da Oracle.

2. **Baixe o Apache Ant:**
   Visite a página de download do Apache Ant e baixe a versão mais recente do arquivo binário (formato ZIP).

3. **Extraia o arquivo baixado:**
   Use um software de extração, como o WinRAR ou o 7-Zip, para extrair o conteúdo do arquivo ZIP para um diretório, por exemplo, `C:\Apache\Ant`.

4. **Configurar variáveis de ambiente:**
   - Abra o Painel de Controle e vá em **Sistema e Segurança** > **Sistema** > **Configurações avançadas do sistema**.
   - Clique em **Variáveis de Ambiente**.
   - Em **Variáveis de sistema**, clique em **Novo** e adicione:
     - Nome da variável: `ANT_HOME`
     - Valor da variável: `C:\Apache\Ant`
   - Encontre a variável `Path` em **Variáveis de sistema**, selecione e clique em **Editar**. Adicione `C:\Apache\Ant\bin` ao final do valor da variável.

5. **Verifique a instalação:**
   Abra um novo Prompt de Comando e digite:
   ```sh
   ant -version
   ```