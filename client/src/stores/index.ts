import useErrors from "@/stores/errors"
import useAssays from "@/stores/assays"
import useCorpora from "@/stores/corpora"
import useDocuments from "@/stores/documents"
import useEvaluation from "@/stores/evaluation"
import useConfusion from "@/stores/evaluation/confusion"
import useDistribution from "@/stores/evaluation/distribution"
import useMetrics from "@/stores/evaluation/metrics"
import useExport from "@/stores/export"
import useJobs from "@/stores/jobs"
import useJobSelection from "@/stores/jobselection"
import useTaggers from "@/stores/taggers"
import useTagsets from "@/stores/tagsets"
import useUser from "@/stores/user"
import useEntities from "@/stores/evaluation/entities"

export default {
    useErrors,
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
    useEntities,
}
