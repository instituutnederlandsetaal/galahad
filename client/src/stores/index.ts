import useApp from "./app"
import useAssays from "./assays"
import useCorpora from "./corpora"
import useDocuments from "./documents"
import useEvaluation from "./evaluation"
import useConfusion from "./evaluation/confusion"
import useDistribution from "./evaluation/distribution"
import useMetrics from "./evaluation/metrics"
import useExport from "./export"
import useJobs from "./jobs"
import useJobSelection from "./jobselection"
import useTaggers from "./taggers"
import useTagsets from "./tagsets"
import useUser from "./user"

export default {
    useApp,
    useAssays,
    useConfusion,
    useCorpora,
    useDistribution,
    useDocuments,
    useExport,
    useMetrics,
    useJobs,
    useJobSelection,
    useUser,
    useEvaluation,
    useTaggers,
    useTagsets,
}
