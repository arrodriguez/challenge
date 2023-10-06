## Blocklist Monitor Command

To execute the command, use the following:

```bash
java -jar blocklist-1.0-SNAPSHOT.jar monitor ./config.yml
```

### Description:

The `monitor` command is designed to download and store the IP blocklist file from a specified GitHub repository. It uses the `blockListPath: $WHATEVERPATH/ipsum.txt` parameter from the `config.yml` to determine the storage location.

The command communicates with the GitHub Event API, specifically looking for events with:
```json
"type": "PushEvent"
```
It checks for events that have only one commit with the message:
```plaintext
'Automatic update'
```
Upon detecting such an event, the command will download the related file and save it. Subsequently, it will return a `versionId` sourced from the event's `eventId`. Additionally, the command provides a boolean value indicating if the file underwent an update.

The output will look something like this:

```json
{"updated":true,"versionId":"32322495816"}
```

You can also provide a previous `versionId` to the command to monitor for changes:

```bash
java -jar blocklist-1.0-SNAPSHOT.jar monitor --version-id <YOUR_VERSION_ID> ./config.yml
```

### Command Help:

```plaintext
usage: java -jar blocklist-1.0-SNAPSHOT.jar
       monitor [-vid VERSION-ID] [-p {true,false}] [-h] [file]

Retrieves the latest blocklist from a GitHub repository, checking against a provided version to ensure updates are captured.

positional arguments:
  file                   application configuration file

named arguments:
  -vid VERSION-ID, --version-id VERSION-ID
                         Version-id of the last ipsum dataset version that was retrieved from the repository
  -p {true,false}, --pretty-print {true,false}
                         Allow the output of VersionID to be pretty printed
  -h, --help             show this help message and exit
```
