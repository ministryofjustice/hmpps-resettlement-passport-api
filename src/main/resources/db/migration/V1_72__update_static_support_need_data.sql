-- Add missing exclude_from_count flag on "no support needs identified" needs.
update support_need set exclude_from_count = true where id in (95, 103, 108);