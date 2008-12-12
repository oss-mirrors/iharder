<?php
header( "Content-type: image/png" );

if( $_GET['cot'] ){
  $img = cotToMil2525b($_GET['cot']) . ".png";
  if( file_exists($img) ){
    $data = file_get_contents($img);
    header( "Content-length: " + sizeof($data) );
    echo $data;
  } // end if: exists
} // end if: cot


function cotToMil2525b( $cot ){
  $m25 = "---------------";
  if( substr($cot,0,1) == 'a' ){
    $affil = strlen($cot) >= 2 ?
      substr($cot,2,1) : 'O';
    $space = strlen($cot) >= 4 ?
      substr($cot,4,1) : 'X';

    for( $i = 6; $i < strlen($cot) && i < 17; $i += 2 ){
      $m25 = substr_replace( $m25, substr($cot,$i,1), 4 + ($i-6)/2, 1); 
    } // end for: detail fields

    $m25 = substr_replace( $m25, 's', 0, 1 );
    $m25 = substr_replace( $m25, $affil, 1, 1 );
    $m25 = substr_replace( $m25, $space, 2, 1 );
    $m25 = substr_replace( $m25, 'p', 3, 1 );

  } // end if: atoms

  return $m25;
}

?>
