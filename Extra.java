//Partial Update (if req. have value change old value otherwise assign old value)

private ProjectEstimation updateWithBuilder(ProjectEstimation estimation, ProjectEstimationRequest request) {

    return estimation.toBuilder()
            .title(request.getTitle() != null?request.getTitle() : estimation.getTitle())
            .project(request.getProject() != null ? request.getProject() : estimation.getProject())
            .phase(request.getPhase() != null ? request.getPhase() : estimation.getPhase())
            .mileStones(request.getMileStones() != null ? request.getMileStones().stream().map(mile -> {
                int totalHours = calculateWorkingHours(
                        mile.getStartDate() != null ? mile.getStartDate() : estimation.getMileStones().get(0).getStartDate(),
                        mile.getEndDate() != null ? mile.getEndDate() : estimation.getMileStones().get(0).getEndDate()
                );
                return MileStone.builder()
                        .title(mile.getTitle() != null ? mile.getTitle() : estimation.getMileStones().get(0).getTitle())
                        .startDate(mile.getStartDate() != null ? mile.getStartDate() : estimation.getMileStones().get(0).getStartDate())
                        .endDate(mile.getEndDate() != null ? mile.getEndDate() : estimation.getMileStones().get(0).getEndDate())
                        .totalHours(totalHours)
                        .build();
            }).toList() : estimation.getMileStones())
            .roles(request.getRoles() != null ? request.getRoles().stream().map(role -> Role.builder()
                    .currency(role.getCurrency() != null ? role.getCurrency() : estimation.getRoles().get(0).getCurrency())
                    .workMode(role.getWorkMode() != null ? role.getWorkMode() : estimation.getRoles().get(0).getWorkMode())
                    .marginRate(role.getMarginRate() != 0 ? role.getMarginRate() : estimation.getRoles().get(0).getMarginRate())
                    .maxRate(role.getMaxRate() != 0 ? role.getMaxRate() : estimation.getRoles().get(0).getMaxRate())
                    .rateApplicableFrom(role.getRateApplicableFrom() != null ? role.getRateApplicableFrom() : estimation.getRoles().get(0).getRateApplicableFrom())
                    .rateApplicableTo(role.getRateApplicableTo() != null ? role.getRateApplicableTo() : estimation.getRoles().get(0).getRateApplicableTo())
                    .build()).toList() : estimation.getRoles())
            .build();
}
