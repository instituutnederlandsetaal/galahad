# Dev info
When debugging locally, add "profile=dev" to the environment variables (e.g. in the intelliJ debug configuration).
This makes it so that:
- taggers are called on localhost, instead of their docker container name via a docker network
- we use a test user, instead of retrieving it from the request headers

# Source code
The src/ folder contains the following packages:

## app
The base for spring boot and spring configuration like handling special JSON serilization.

## web
The Endpoints class lists all API endpoints.

## files
All Galahad data is stored on disk. For example, DiskValue<DocumentMetadata> stores document metadata in JSON on disk. 
Similarly, ValidatedDiskValue<CorpusMetadata> stores CorpusMetadata. However, on retrieval it first performs a isValid() check (which checks disk modification).
CorpusMetadata stores the number of documents, so if a new file has been added, it should not be valid and is recalculated

A corpus or document is represented by a folder on disk. This is the GalahadFolder class.

## annotations
The smallest unit of information in Galahad is an annotation, e.g. the part of speech "NOU-C(number=sg)".
Annotations are bundled together on a Term. The TEI XML `<w lemma="hello" pos="INT">hello</w>` results in a Term with 3 annotations:
- token: hello (token is also an annotation)
- lemma: hello
- pos: INT

Terms are contained in a SentenceLayer (TEI XML `<s>`). Span annotations are defined on this level too. For example, the named entity "LOC" defined as a span over two tokens: "The", "Netherlands".

Sentences are contained in a ParagraphLayer (TEI XML `<p>`). Paragraphs are contained in a DocumentLayer (TEI XML `<text>`).
We are not done yet however, as a single _file_ can contain multiple _documents_ in formats like TEI and CoNLL-U. And so, documents are contained in a Layer. This is the main class that is used. 

A Layer provides a summary: this is the number of annotations of each type. It also provides a preview of the first couple of terms.
Converting a layer to plaintext is as simple as .toString().

## corpora

## documents

## exceptions
A bunch of Galahad-specific exceptions that include a HTTP status.

## export

## formats
Contains all document readers, converters and mergers for the supported formats in Galahad.

## evaluation
For evaluating a single layer (the frequency distribution) or comparing two layers (part of speech confusion and accuracy metrics), where one represents the absolute truth (called the "reference") and one is being tested against it (called the "hypothesis"). The main use case is setting the sourceLayer as the absolute truth reference.

The subpackages confusion, distribution, and metrics calculate their respective evaluations in a similar manner.
E.g. for distribution: There is a CorpusDistribution, which calls a DocumentDistribution on each document in the corpus.
Both types inherit from the same aggregation class. Within each document, term are aggregated into a single object. And within the corpus, documents are aggregated into a single object.

![Diagram of downloading a CorpusConfusion](docs/CorpusConfusion.png)

### evaluation.comparison
To perform the evaluation, we compare layers (LayerComparison), which requires us to compare the terms (TermComparison), which requires us to compare the word forms (WordFormComparions). The part of speech confusion and accuracy metrics then use these comparisons to construct an evaluation. They also keep track of 10 random samples for each evaluation. For example, for the %-incorrect metric of lemmata, 10 random samples are chosen that show a term with an incorrect lemma. And, for example, for the part of speech confusion, 10 samples are chosen to demonstrate the evaluation "noun vs verb". Etc.

Both evaluation.metrics and evaluation.comparison group on some annotation (e.g. pos). You can filter this even further

### evaluation.metrics
The metrics calculation does in part use the same aggregation as described above, but adds more complexity in order to keep track of false positives, false negatives, etc.; micro and macro metrics; grouped metrics and global metrics; and all of this grouped by various annotations (lemma, pos, etc.) and filtered by various criteria (e.g. multi pos only, like ADP+NOU).

CorpusMetrics and DocumentMetrics exist and inherit from Metrics. Because of the group and filter option, we need to define these settings somewhere: MetricsSettings.

Using MetricsSettings, you can create a MetricsType, which calculates metrics according to the settings. The Metrics base class, then, calculates a list of different MetricsTypes all at the same time.

These MetricsTypes have global information: micro, macro, classification classes (true positive, etc.). And they have grouped information (e.g. grouped by pos: NOU, ADV, etc.): micro and classification classes. 

The global information is calculated based on the grouped information at the time of json serialization (at that point, all terms in the corpus have been seen).

### evaluation.assays
In order to show a leaderboard of taggers on datasets, we have so-called 'assays'. These are simplified accuracy metrics.

## jobs
The process of a tagger tagging a document, which creates a new annotation layer, is called a job.

## tagset & tagger
Both relatively simple packages. Read out yaml files in a folder and make them available in a singleton-like manner.

