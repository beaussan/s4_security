cryptochat

## Installer gradle

Si vous êtes sur une machine avec le proxy de Lille 1 : ` ./gradleWProxy `

sinon si vous êtes sous windows :  ` gradlew.bat `

sinon : ` ./gradlew `

### Tasks

#### pour démarer le serveur :
`
./gradlew serv
`

#### pour démarer un client :
`
./gradlew client
`

#### pour générer la javadoc :
`
./gradlew javadoc
`

Il se trouve dans : build/docs

#### pour générer un zip distribuable :

`
./gradlew distZip
`


Il se trouve dans /build/distributions


## Importer dans un IDE

### Eclipse

`
gradlew eclipse
`
pour générer les .classpath et .project puis il suffit d'ouvrir le projet dans Eclipse

Ou le plugin http://www.vogella.com/tutorials/EclipseGradle/article.html existe.

### IntelliJ IDEA

ouvrir le build.gradle