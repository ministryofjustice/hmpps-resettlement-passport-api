package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway

data class ResettlementAssessmentQuestionAndAnswer<T> (
  val question: ResettlementAssessmentQuestions,
  val answer: T
)

enum class ResettlementAssessmentQuestions {
  WHERE_WILL_THEY_LIVE,
  WHO_WILL_THEY_LIVE_WITH,
  ACCOM_CRS,
  CHECK_ANSWERS,
  COUNCIL_AREA,
  COUNCIL_AREA_REASON
}

enum class TypeOfQuestion {
  RADIO_WITH_ADDRESS,
  LIST_OF_RESIDENTS,
  RADIO
}

interface AssessmentPage {

}

enum class AttitudeAssessmentPage {
  ANGER_MANAGEMENT {
    override val pathway: Pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR
  },
  ARE_THEY_INFLUENCED {
    override val pathway: Pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR
  },
  ISSUES_WITH_GAMBLING {
    override val pathway: Pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR
  },
  GANG_INVOLVEMENT {
    override val pathway: Pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR
  },
  GANG_INVOLVEMENT_UNDER_THREAT {
    override val pathway: Pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR
  },
  VICTIM_OF_SEXUAL_VIOLENCE {
    override val pathway: Pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR
  },
  SEXUAL_VIOLENCE_DETAILS {
    override val pathway: Pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR
  },
  VICTIM_OF_DOMESTIC_VIOLENCE {
    override val pathway: Pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR
  },
  DOMESTIC_VIOLENCE_DETAILS {
    override val pathway: Pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR
  };
  abstract val pathway: Pathway
}

enum class AccommodationAssessmentPage : AssessmentPage {
  WHERE_WILL_THEY_LIVE {
    override val pathway: Pathway = Pathway.ACCOMMODATION
  },
  WHO_WILL_THEY_LIVE_WITH {
    override val pathway: Pathway = Pathway.ACCOMMODATION
  },
  CONSENT_FOR_CRS {
    override val pathway: Pathway = Pathway.ACCOMMODATION
  },
  WHAT_COUNCIL_AREA {
    override val pathway: Pathway = Pathway.ACCOMMODATION
  },
  CHECK_ANSWERS {
    override val pathway: Pathway = Pathway.ACCOMMODATION
  };
  abstract val pathway: Pathway
}

data class ResettlementAssessmentQuestion (
  val id: ResettlementAssessmentQuestions,
  val displayText: String,
  val type: TypeOfQuestion,
  val nextPage: (currentPage: AssessmentPage, questions:List<ResettlementAssessmentQuestionAndAnswer<*>>) -> AssessmentPage,
)

data class ResettlementAssessmentNode (
  val assessmentPage: AssessmentPage,
  val nextPage: (currentPage: AssessmentPage, questions:List<ResettlementAssessmentQuestionAndAnswer<*>>) -> AssessmentPage,
)