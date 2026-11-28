/*
 * ZyNova Launcher — 三份声明的正文（西班牙语 / 巴西葡萄牙语）
 * GPL-3.0-or-later
 *
 * 西语：正式体（usted）；法律名称按当地官方写法，不翻译专有名词。
 * 葡语（巴西）：正式体（você）；术语按巴西软件惯例（save = partida salva、modpack = modpack）。
 */
package com.movtery.zalithlauncher.onboarding

// ─────────────────────────────────────────────────────────────────
//  Español
// ─────────────────────────────────────────────────────────────────

internal val TERMS_ES = """
《Contrato de licencia de usuario final + Política de privacidad》

1. Aceptación
Al instalar, copiar o usar ZyNova Launcher («el Software») usted confirma que ha leído, comprendido y aceptado íntegramente este contrato. Si no está de acuerdo, deje de usarlo y desinstálelo.

2. Licencia y código abierto
El Software se distribuye bajo GNU GPL-3.0 y su código es público. Puede usarlo, modificarlo y redistribuirlo conforme a la GPL-3.0; al redistribuir debe incluir la licencia y los avisos de copyright, publicar el código correspondiente y no añadir restricciones adicionales. El Software es un lanzador de terceros **no oficial**, sin relación de afiliación, representación, patrocinio ni respaldo con Mojang Studios, Microsoft, NetEase ni sus filiales. «Minecraft» es una marca de Mojang Synergies AB.

3. Cuentas y archivos de juego
El Software no proporciona, vende ni comparte cuentas de Minecraft. Debe disponer de una cuenta legítima propia, o usar el modo sin conexión únicamente para archivos de juego que posea legalmente. Los derechos sobre el juego, los mods, los modpacks, los paquetes de recursos y los shaders pertenecen a sus respectivos titulares.

4. Sin garantías y limitación de responsabilidad
El Software se ofrece «tal cual», sin garantías expresas ni implícitas. En la máxima medida permitida por la ley, los autores y colaboradores no responden de ningún daño directo, indirecto, incidental, especial o consecuente derivado del uso o de la imposibilidad de uso, incluida la pérdida de partidas, mundos o ajustes, el sobrecalentamiento o las ralentizaciones del dispositivo, el bloqueo de cuentas y los cambios o cierres de servicios de terceros. Haga copias de seguridad de sus partidas importantes.

5. Restricciones de uso
No puede usar el Software para actividades ilícitas, para vulnerar derechos de terceros, para atacar redes o dispositivos ajenos, ni para eludir los términos de servicio de una plataforma o las medidas de protección de menores.

6. Servicios de terceros y costes
El chat con IA, la búsqueda web, el túnel y el multijugador dependen de servicios de terceros y pueden requerir sus propias claves y cuotas. Los costes y las condiciones se resuelven entre usted y el proveedor correspondiente.

7. Privacidad
1. Por defecto no se recogen ni se envían datos personales; las cuentas, instancias, ajustes y registros permanecen en su dispositivo.
2. Los datos solo salen del dispositivo si usted activa estas funciones: IA (mensajes y parámetros de herramientas al proveedor de modelo configurado), búsqueda web (consultas al motor de búsqueda), túnel (claves e información del túnel al nodo del servicio), envío de registros (solo al pulsar).
3. No se recogen, solicitan ni envían números de identidad, imágenes faciales ni teléfonos. Si en el futuro hiciera falta verificar la edad, el cálculo se haría solo en memoria, sin guardar ni transmitir.
4. Permisos: almacenamiento (leer y escribir el directorio del juego, exportar registros), notificaciones (progreso de descargas y túnel), red (descargas, IA, multijugador). No se solicitan micrófono ni cámara.
5. Puede consultar, exportar y eliminar sus datos locales en cualquier momento, o desinstalar la aplicación para borrarlos todos.

8. Cambios y terminación
Este contrato puede cambiar con las versiones; **solo se exige un nuevo consentimiento si los términos cambian de forma sustancial**. El incumplimiento extingue la licencia automáticamente: deje de usar el Software y elimine todas las copias.

9. Varios
La nulidad de una cláusula no afecta a las demás. Si entra en conflicto con normas imperativas locales, prevalecerá la ley.
""".trimIndent()

internal val LICENSES_ES = """
《Avisos de componentes de código abierto + Avisos de riesgo adicionales》

1. El propio Software
GPL-3.0; el texto completo está en el archivo LICENSE del repositorio. Se conservan íntegramente los avisos de copyright de los proyectos de origen ZalithLauncher2 / Fold Craft Launcher / PojavLauncher.

2. Componente con las obligaciones más estrictas
Terracotta (multijugador) = AGPL-3.0, redistribuido sin modificaciones. La AGPL es más estricta que la GPL: si lo modifica y redistribuye, o expone su funcionalidad como servicio de red, debe publicar el código fuente correspondiente.

3. Componentes con copyleft débil
MobileGlues (LGPL-2.1), sora-editor / language-textmate (LGPL-2.1), LTW-Turbo (LGPL-3.0), TouchController (LGPL-3.0) — se distribuyen como bibliotecas dinámicas o módulos independientes y usted puede sustituirlos.

4. Componentes con licencias permisivas
MIT: GL4ES, ByteHook, OpenNBT, skinview3d; BSD-3-Clause: ANGLE, LWJGL, MMKV; Zlib: SDL3, sdl2-compat; EPL-2.0: tm4e; 0BSD: XZ for Java; Apache-2.0: frp / frpc, AndroidX, Ktor, OkHttp y otros. La lista detallada y los textos completos están en los avisos siguientes y en Ajustes → Acerca de → Licencias de código abierto.

5. Avisos de riesgo adicionales
1. Las partidas, mundos, capturas, ajustes y mods pueden perderse o dañarse por cierres inesperados, cortes de luz, fallos de almacenamiento o conflictos con otro software;
2. El dispositivo puede calentarse, consumir más batería, ralentizarse o sufrir la finalización de procesos en segundo plano;
3. Usar cuentas, servidores o servicios de terceros puede provocar bloqueos, limitaciones, cobros o tratamiento de datos;
4. Los mods o modpacks de origen desconocido pueden contener código malicioso;
5. El túnel expone puertos locales a Internet y puede ser escaneado o abusado.
Usted evalúa y asume estos riesgos; el Software no promete copias de seguridad, recuperación ni compensación.

6. Qué puede hacer
Uso y modificación personales: permitidos. Redistribución: permitida con licencia y avisos de copyright, publicación del código y atribución de los proyectos de origen. Uso comercial con código cerrado: no permitido. Redistribuir componentes AGPL modificados: obligatorio publicar los cambios.
""".trimIndent()

internal val GUIDE_ES = """
《Primeros pasos + Regiones y ley aplicable》

1. Primer arranque
Acepte los tres documentos y elija su grupo de edad. En la pantalla de inicio pulse «Descargar una instancia», elija una versión (opcionalmente con Fabric / NeoForge / Quilt) y espere a que termine la descarga.

2. Mods, paquetes de recursos, shaders y modpacks
La página de Descargas admite Modrinth / CurseForge y resuelve las dependencias automáticamente. También puede colocar los archivos en la carpeta correcta o usar «Compartir con ZyNova».

3. Multijugador
Terracotta: ambos jugadores instalan este lanzador y entran en la misma sala (P2P, sin abrir puertos). Túnel Sakura: requiere cuenta de Sakura y clave de acceso; el túnel es solo TCP y el puerto local es 25565 por defecto (modificable).

4. Asistente de IA
Puede instalar el juego o mods, leer registros, cambiar ajustes y generar archivos Word / PPT. La búsqueda web usa DeepSeek o DuckDuckGo; los archivos borrados van a la papelera.

5. Si algo falla
Ajustes → Acerca de → Registros para consultar y exportar; tras un cierre inesperado, abra el informe o pida a la IA que lo analice. Al reportar, incluya modelo del dispositivo, versión de Android, versión del lanzador y el archivo de registro.

6. Consejos de rendimiento
Mantenga la memoria por debajo de la mitad de la RAM disponible; pruebe primero MobileGlues / LTW-Turbo y use GL4ES si hay problemas de compatibilidad; guarde los modpacks grandes en el almacenamiento interno con espacio suficiente.

7. Desinstalación y copias de seguridad
Desinstalar no borra los archivos de juego del almacenamiento externo; elimine las instancias desde la aplicación. Exporte una copia del modpack antes de actualizar.
""".trimIndent()

// ─────────────────────────────────────────────────────────────────
//  Português (Brasil)
// ─────────────────────────────────────────────────────────────────

internal val TERMS_PT_BR = """
《Contrato de licença de usuário final + Política de privacidade》

1. Aceitação
Ao instalar, copiar ou usar o ZyNova Launcher («o Software») você confirma que leu, entendeu e aceitou integralmente este contrato. Se não concordar, interrompa o uso e desinstale o Software.

2. Licença e código aberto
O Software é distribuído sob a GNU GPL-3.0 e seu código é público. Você pode usá-lo, modificá-lo e redistribuí-lo conforme a GPL-3.0; ao redistribuir, é preciso incluir a licença e os avisos de copyright, publicar o código correspondente e não impor restrições adicionais. O Software é um launcher de terceiros **não oficial**, sem vínculo de afiliação, representação, patrocínio ou endosso com a Mojang Studios, a Microsoft, a NetEase ou suas subsidiárias. «Minecraft» é marca da Mojang Synergies AB.

3. Contas e arquivos de jogo
O Software não fornece, vende nem compartilha contas do Minecraft. Você precisa de uma conta legítima própria ou usar o modo offline apenas para arquivos de jogo que possui legalmente. Os direitos sobre o jogo, mods, modpacks, pacotes de recursos e shaders pertencem aos respectivos titulares.

4. Sem garantias e limitação de responsabilidade
O Software é fornecido «como está», sem garantias expressas ou implícitas. Na máxima extensão permitida por lei, autores e colaboradores não respondem por danos diretos, indiretos, incidentais, especiais ou consequenciais decorrentes do uso ou da impossibilidade de uso, incluindo perda de partidas salvas, mundos ou ajustes, superaquecimento ou travamentos do aparelho, bloqueio de contas e mudanças ou encerramento de serviços de terceiros. Faça você mesmo o backup das partidas importantes.

5. Restrições de uso
Você não pode usar o Software para atividades ilícitas, para violar direitos de terceiros, para atacar redes ou aparelhos alheios, nem para burlar termos de serviço de plataformas ou medidas de proteção de menores.

6. Serviços de terceiros e custos
Chat com IA, busca na web, túnel e multijogador dependem de serviços de terceiros e podem exigir chaves e cotas próprias. Custos e condições são resolvidos entre você e o fornecedor correspondente.

7. Privacidade
1. Por padrão, nenhum dado pessoal é coletado ou enviado; contas, instâncias, ajustes e logs ficam no seu aparelho.
2. Os dados só saem do aparelho se você ativar estas funções: IA (mensagens e parâmetros de ferramentas para o provedor de modelo configurado), busca na web (consultas para o mecanismo de busca), túnel (chaves e informações do túnel para o nó do serviço), envio de logs (somente ao tocar).
3. Números de documento, imagens faciais e telefones não são coletados, solicitados nem enviados. Se uma verificação de idade for necessária no futuro, o cálculo ocorrerá apenas na memória, sem gravar nem transmitir.
4. Permissões: armazenamento (ler e gravar o diretório do jogo, exportar logs), notificações (progresso de downloads e do túnel), rede (downloads, IA, multijogador). Microfone e câmera não são solicitados.
5. Você pode consultar, exportar e excluir os dados locais a qualquer momento, ou desinstalar o aplicativo para remover tudo.

8. Alterações e rescisão
Este contrato pode mudar com as versões; **novo consentimento só é exigido quando os termos mudarem de forma substancial**. O descumprimento encerra a licença automaticamente: pare de usar o Software e exclua todas as cópias.

9. Disposições gerais
A nulidade de uma cláusula não afeta as demais. Em conflito com normas imperativas locais, prevalece a lei.
""".trimIndent()

internal val LICENSES_PT_BR = """
《Avisos de componentes de código aberto + Avisos de risco adicionais》

1. O próprio Software
GPL-3.0; o texto completo está no arquivo LICENSE do repositório. Os avisos de copyright dos projetos de origem ZalithLauncher2 / Fold Craft Launcher / PojavLauncher são integralmente preservados.

2. Componente com as obrigações mais rígidas
Terracotta (multijogador) = AGPL-3.0, redistribuído sem modificações. A AGPL é mais rígida que a GPL: se você o modificar e redistribuir, ou oferecer sua funcionalidade como serviço de rede, deve publicar o código-fonte correspondente.

3. Componentes com copyleft fraco
MobileGlues (LGPL-2.1), sora-editor / language-textmate (LGPL-2.1), LTW-Turbo (LGPL-3.0), TouchController (LGPL-3.0) — distribuídos como bibliotecas dinâmicas ou módulos separados, substituíveis por você.

4. Componentes com licenças permissivas
MIT: GL4ES, ByteHook, OpenNBT, skinview3d; BSD-3-Clause: ANGLE, LWJGL, MMKV; Zlib: SDL3, sdl2-compat; EPL-2.0: tm4e; 0BSD: XZ for Java; Apache-2.0: frp / frpc, AndroidX, Ktor, OkHttp e outros. A lista detalhada e os textos completos estão nos avisos abaixo e em Configurações → Sobre → Licenças de código aberto.

5. Avisos de risco adicionais
1. Partidas salvas, mundos, capturas, ajustes e mods podem ser perdidos ou corrompidos por falhas, queda de energia, defeito de armazenamento ou conflito com outros programas;
2. O aparelho pode esquentar, consumir mais bateria, travar ou ter processos em segundo plano encerrados pelo sistema;
3. Usar contas, servidores ou serviços de terceiros pode causar bloqueios, limites, cobranças ou tratamento de dados;
4. Mods ou modpacks de origem desconhecida podem conter código malicioso;
5. O túnel expõe portas locais à internet e pode ser escaneado ou abusado.
Você avalia e assume esses riscos; o Software não promete backup, recuperação ou indenização.

6. O que você pode fazer
Uso e modificação pessoais: permitidos. Redistribuição: permitida com licença e avisos de copyright, publicação do código e atribuição aos projetos de origem. Uso comercial com código fechado: não permitido. Redistribuir componentes AGPL modificados: publicação das alterações obrigatória.
""".trimIndent()

internal val GUIDE_PT_BR = """
《Primeiros passos + Regiões e lei aplicável》

1. Primeira execução
Aceite os três documentos e escolha sua faixa etária. Na tela inicial, toque em «Baixar uma instância», escolha uma versão (opcionalmente com Fabric / NeoForge / Quilt) e aguarde o download.

2. Mods, pacotes de recursos, shaders e modpacks
A página de Downloads aceita Modrinth / CurseForge e resolve dependências automaticamente. Você também pode colocar os arquivos na pasta correta ou usar «Compartilhar com o ZyNova».

3. Multijogador
Terracotta: os dois jogadores instalam este launcher e entram na mesma sala (P2P, sem abrir portas). Túnel Sakura: exige conta Sakura e chave de acesso; o túnel é somente TCP e a porta local padrão é 25565 (alterável).

4. Assistente de IA
Pode instalar o jogo ou mods, ler logs, alterar ajustes e gerar arquivos Word / PPT. A busca na web usa DeepSeek ou DuckDuckGo; arquivos apagados vão para a lixeira.

5. Se algo der errado
Configurações → Sobre → Logs para ver e exportar; após uma falha, abra o relatório ou peça à IA para analisar. Ao reportar, inclua modelo do aparelho, versão do Android, versão do launcher e o arquivo de log.

6. Dicas de desempenho
Mantenha a memória abaixo da metade da RAM disponível; teste primeiro MobileGlues / LTW-Turbo e use GL4ES em caso de incompatibilidade; mantenha modpacks grandes no armazenamento interno com espaço suficiente.

7. Desinstalação e backup
Desinstalar não apaga os arquivos de jogo no armazenamento externo; exclua as instâncias pelo aplicativo. Exporte um backup do modpack antes de atualizar.
""".trimIndent()

// ─────────────────────────────────────────────────────────────────
//  本地区补充条款（西语 / 葡语）
// ─────────────────────────────────────────────────────────────────

internal val JURIS_TERMS_ES = """
Suplemento regional — {region}

1. Datos personales
En esta región se aplica {law}; la autoridad de control es {regulator}. Usted tiene derecho a acceder, rectificar, suprimir, retirar el consentimiento, limitar el tratamiento y reclamar conforme a la ley local. Por defecto no se recoge nada fuera de su dispositivo; que los datos salgan depende únicamente de las funciones de terceros que active.

2. Transferencia transfronteriza
Los datos solo se transfieren si activa una función de terceros (modelo de IA, motor de búsqueda, retransmisión multijugador, túnel). En ese caso los trata el proveedor conforme a su propia política de privacidad, fuera del control del autor.

3. Menores
En esta región se considera la mayoría de edad a los {age} años.{timeWindow}
""".trimIndent()

internal val JURIS_LICENSES_ES = """
Suplemento regional — {region}

1. Eficacia de las obligaciones de licencia
Las obligaciones de GPL-3.0 / AGPL-3.0 / LGPL se interpretan y ejecutan conforme a la ley local; el artículo 13 de la AGPL-3.0 (publicación del código al ofrecer el software como servicio de red) es exigible en la mayoría de jurisdicciones que reconocen el derecho de autor.

2. Control de exportaciones y sanciones
No puede usar este producto en países o regiones sancionados, por personas o entidades sancionadas, ni con fines contrarios a las normas de control de exportaciones o sanciones.

3. Disponibilidad de servicios de terceros
La disponibilidad, los precios y los requisitos de cumplimiento de modelos de IA, motores de búsqueda, túneles y retransmisiones multijugador los decide cada proveedor y pueden no estar disponibles o estar restringidos en su región.
""".trimIndent()

internal val JURIS_GUIDE_ES = """
Suplemento regional — {region}

1. Ley aplicable y jurisdicción
Las controversias entre usted y este producto se rigen por las leyes de {region} y las conocen los tribunales competentes de su domicilio. No se ven afectados los derechos imperativos de consumo que le reconozca la ley local.

2. Menores
En esta región se considera la mayoría de edad a los {age} años.{timeWindow}

3. Idioma y detección de región
El idioma del texto sigue el idioma del sistema; las cláusulas regionales siguen el país y la zona horaria del sistema. Ambos deben estar admitidos para entrar a la pantalla principal.

4. Si le bloquean por error
La pantalla de bloqueo muestra el criterio aplicado (país / zona horaria / idioma). Si está en una región admitida y aun así se le bloquea, adjunte esa línea en un Issue de GitHub.
""".trimIndent()

internal val JURIS_TERMS_PT_BR = """
Suplemento regional — {region}

1. Dados pessoais
Nesta região aplica-se {law}; a autoridade de controle é {regulator}. Você tem direito de acessar, corrigir, eliminar, revogar o consentimento, limitar o tratamento e reclamar conforme a lei local. Por padrão, nada é coletado fora do seu aparelho; a saída de dados depende apenas das funções de terceiros que você ativar.

2. Transferência transfronteiriça
Os dados só são transferidos se você ativar uma função de terceiros (modelo de IA, mecanismo de busca, retransmissão multijogador, túnel). Nesse caso são tratados pelo fornecedor conforme a política de privacidade dele, fora do controle do autor.

3. Menores
Nesta região, considera-se maioridade a partir de {age} anos.{timeWindow}
""".trimIndent()

internal val JURIS_LICENSES_PT_BR = """
Suplemento regional — {region}

1. Eficácia das obrigações de licença
As obrigações da GPL-3.0 / AGPL-3.0 / LGPL são interpretadas e aplicadas conforme a lei local; o artigo 13 da AGPL-3.0 (publicação do código ao oferecer o software como serviço de rede) é exigível na maioria das jurisdições que reconhecem direito autoral.

2. Controle de exportação e sanções
Você não pode usar este produto em países ou regiões sancionados, por pessoas ou entidades sancionadas, nem para fins contrários às regras de controle de exportação ou sanções.

3. Disponibilidade de serviços de terceiros
Disponibilidade, preços e requisitos de conformidade de modelos de IA, mecanismos de busca, túneis e retransmissões multijogador são definidos por cada fornecedor e podem estar indisponíveis ou restritos na sua região.
""".trimIndent()

internal val JURIS_GUIDE_PT_BR = """
Suplemento regional — {region}

1. Lei aplicável e foro
As controvérsias entre você e este produto regem-se pelas leis de {region} e são julgadas pelos tribunais competentes do seu domicílio. Os direitos imperativos do consumidor previstos na lei local não são afetados.

2. Menores
Nesta região, considera-se maioridade a partir de {age} anos.{timeWindow}

3. Idioma e detecção de região
O idioma do texto segue o idioma do sistema; as cláusulas regionais seguem o país e o fuso horário do sistema. Ambos precisam estar na lista suportada para entrar na tela principal.

4. Se você for bloqueado por engano
A tela de bloqueio mostra o critério usado (país / fuso horário / idioma). Se estiver em uma região suportada e ainda assim for bloqueado, anexe essa linha a uma Issue no GitHub.
""".trimIndent()
