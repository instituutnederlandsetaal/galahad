import useApp from "./app"
import useUser from "./user"
import useCorpora from "./corpora"
import useDocuments from "./documents"
import useJobs from "./jobs"
import useJobSelection from "./jobselection"
import useTaggers from "./taggers"
import useTagsets from "./tagsets"
import useAssays from "./assays"
import useEvaluation from "./evaluation"
import useDistribution from "./evaluation/distribution"
import useMetrics from "./evaluation/metrics"
import useConfusion from "./evaluation/confusion"
import useExport from "./export"

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
