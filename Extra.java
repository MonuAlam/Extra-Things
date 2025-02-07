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
//=================================================================================================
  //for authentication in db roles are not in the form of ROLE_USER,ROLE_ADMIN then use it

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
	        throws ServletException, IOException {

	    String authHeader = request.getHeader("Authorization");
	    String token = null;
	    String username = null;

	    // Extract the token from the Authorization header
	    if (authHeader != null && authHeader.startsWith("Bearer ")) {
	        token = authHeader.substring(7);
	        username = jwtService.extractUserName(token); // Extract username from token
	    }

	    // If username is extracted and no authentication is set in SecurityContext
	    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
	        UserDetails userDetails = 
	                context.getBean(CustomUserDetailsService.class)
	                .loadUserByUsername(username);

	        // Validate the token
	        if (jwtService.validateToken(token, userDetails)) {
	            // Add ROLE_ prefix to roles if needed
	            List<SimpleGrantedAuthority> authorities = userDetails.getAuthorities().stream()
	                    .map(authority -> new SimpleGrantedAuthority(
	                            authority.getAuthority().startsWith("ROLE_") ? 
	                            authority.getAuthority() : "ROLE_" + authority.getAuthority()))
	                    .collect(Collectors.toList());

	            // Create an authentication token with prefixed roles
	            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
	                    userDetails, null, authorities);

	            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

	            // Set the authentication in SecurityContext
	            SecurityContextHolder.getContext().setAuthentication(authToken);
	        }
	    }

	    // Proceed with the filter chain
	    filterChain.doFilter(request, response);
	}

   @Bean
   public AuthenticationProvider authenticationProvider() {
       DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
       provider.setPasswordEncoder(passwordEncoder());
       provider.setUserDetailsService(userDetailsService);
       provider.setAuthoritiesMapper(grantedAuthorities -> 
           grantedAuthorities.stream()
               .map(authority -> new SimpleGrantedAuthority("ROLE_" + authority.getAuthority())) // Add prefix dynamically
               .toList()
       );
       return provider;
   }  

